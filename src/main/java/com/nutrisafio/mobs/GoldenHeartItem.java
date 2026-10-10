package com.nutrisafio.mobs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Corazon Dorado: al comerlo te sube 1 corazon de vida maxima PARA SIEMPRE.
 * Se guarda como un modificador de vida en el jugador, y se conserva al morir.
 * Tope: 10 corazones extra (para que no se rompa el juego).
 */
public class GoldenHeartItem extends Item {
    public static final ResourceLocation MODIFIER_ID = NutrisafioMobs.id("corazones_extra");
    public static final int MAX_EXTRA_HEARTS = 10;

    public GoldenHeartItem(Properties properties) {
        super(properties);
    }

    /** Cuantos corazones extra tiene este jugador. */
    public static int extraHearts(LivingEntity entity) {
        AttributeInstance inst = entity.getAttribute(Attributes.MAX_HEALTH);
        if (inst == null) return 0;
        AttributeModifier mod = inst.getModifier(MODIFIER_ID);
        return mod == null ? 0 : (int) Math.round(mod.amount() / 2.0);
    }

    /** Pone exactamente N corazones extra. */
    public static void setExtraHearts(LivingEntity entity, int hearts) {
        AttributeInstance inst = entity.getAttribute(Attributes.MAX_HEALTH);
        if (inst == null) return;
        inst.removeModifier(MODIFIER_ID);
        if (hearts > 0) {
            inst.addPermanentModifier(new AttributeModifier(MODIFIER_ID, hearts * 2.0,
                    AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (extraHearts(player) >= MAX_EXTRA_HEARTS) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Ya tienes el maximo de corazones extra ("
                        + MAX_EXTRA_HEARTS + ")").withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            int now = Math.min(extraHearts(player) + 1, MAX_EXTRA_HEARTS);
            setExtraHearts(player, now);
            player.heal(2.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            player.displayClientMessage(Component.literal("+1 corazon de vida (" + now + " extra)")
                    .withStyle(ChatFormatting.GOLD), true);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
