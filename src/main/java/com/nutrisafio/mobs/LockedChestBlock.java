package com.nutrisafio.mobs;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Cofre con Candado: solo quien lo puso puede abrirlo o romperlo.
 * Si pones dos juntos, mirando al mismo lado y del mismo dueno, se unen en un cofre doble (54 espacios).
 * Agachado (shift) lo pones sin unirlo, igual que un cofre normal.
 */
public class LockedChestBlock extends BaseEntityBlock {
    public static final MapCodec<LockedChestBlock> CODEC = simpleCodec(LockedChestBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;

    public LockedChestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }

    /** Hacia donde esta la otra mitad (igual que el cofre normal). */
    public static Direction getConnectedDirection(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(TYPE) == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        ChestType type = ChestType.SINGLE;
        Player player = context.getPlayer();
        boolean sneaking = player != null && player.isSecondaryUseActive();
        if (!sneaking && player != null) {
            if (canPair(context, facing, facing.getClockWise(), player.getUUID())) {
                type = ChestType.LEFT;
            } else if (canPair(context, facing, facing.getCounterClockWise(), player.getUUID())) {
                type = ChestType.RIGHT;
            }
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(TYPE, type);
    }

    /** Se puede unir si al lado hay un cofre con candado solo, mirando igual, y del mismo dueno. */
    private boolean canPair(BlockPlaceContext context, Direction facing, Direction side, UUID player) {
        BlockPos other = context.getClickedPos().relative(side);
        BlockState state = context.getLevel().getBlockState(other);
        if (!state.is(this) || state.getValue(TYPE) != ChestType.SINGLE || state.getValue(FACING) != facing) {
            return false;
        }
        return context.getLevel().getBlockEntity(other) instanceof LockedChestBlockEntity chest
                && chest.isOwner(player);
    }

    /** Mantiene unidas (o separa) las dos mitades cuando cambia el bloque de al lado. */
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (neighbor.is(this) && direction.getAxis().isHorizontal()) {
            ChestType other = neighbor.getValue(TYPE);
            if (state.getValue(TYPE) == ChestType.SINGLE && other != ChestType.SINGLE
                    && state.getValue(FACING) == neighbor.getValue(FACING)
                    && getConnectedDirection(neighbor) == direction.getOpposite()) {
                return state.setValue(TYPE, other.getOpposite());
            }
        } else if (state.getValue(TYPE) != ChestType.SINGLE && getConnectedDirection(state) == direction) {
            return state.setValue(TYPE, ChestType.SINGLE);
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LockedChestBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof LockedChestBlockEntity chest) {
            chest.setOwner(player);
        }
    }

    /** La otra mitad, si es cofre doble. */
    private static LockedChestBlockEntity partner(BlockState state, Level level, BlockPos pos) {
        if (state.getValue(TYPE) == ChestType.SINGLE) return null;
        BlockPos other = pos.relative(getConnectedDirection(state));
        BlockState otherState = level.getBlockState(other);
        if (!otherState.is(state.getBlock()) || otherState.getValue(TYPE) == ChestType.SINGLE) return null;
        return level.getBlockEntity(other) instanceof LockedChestBlockEntity chest ? chest : null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                            BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof LockedChestBlockEntity chest)) return InteractionResult.CONSUME;
        LockedChestBlockEntity other = partner(state, level, pos);

        boolean allowed = chest.isAllowed(player) && (other == null || other.isAllowed(player));
        if (!allowed) {
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Este cofre tiene candado. Es de "
                    + chest.getOwnerName()).withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        if (other == null) {
            player.openMenu(chest);
        } else {
            // La mitad "derecha" va arriba, como en el cofre doble normal
            LockedChestBlockEntity first = state.getValue(TYPE) == ChestType.RIGHT ? chest : other;
            LockedChestBlockEntity second = first == chest ? other : chest;
            CompoundContainer both = new CompoundContainer(first, second);
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> ChestMenu.sixRows(id, inventory, both),
                    Component.translatable("container.nutrisafio_mobs.locked_chest_double")));
        }
        level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
        return InteractionResult.CONSUME;
    }

    /** Solo el dueno (o un operador en creativo) puede romperlo. */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof LockedChestBlockEntity chest && !chest.isAllowed(player)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    /** Al romperlo, sus cosas caen al suelo como en un cofre normal. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof LockedChestBlockEntity chest) {
                Containers.dropContents(level, pos, chest);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
