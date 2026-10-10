package com.nutrisafio.mobs;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Inventario del Cofre con Candado (27 espacios, como un cofre normal).
 * Guarda quien lo puso. Las tolvas no pueden sacar ni meter cosas.
 */
public class LockedChestBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private static final int[] NO_SLOTS = new int[0];

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private UUID owner;
    private String ownerName = "";

    public LockedChestBlockEntity(BlockPos pos, BlockState state) {
        super(NutrisafioMobs.LOCKED_CHEST_ENTITY, pos, state);
    }

    public void setOwner(Player player) {
        this.owner = player.getUUID();
        this.ownerName = player.getGameProfile().getName();
        this.setChanged();
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public boolean isOwner(UUID player) {
        return this.owner == null || this.owner.equals(player);
    }

    /** El dueno siempre puede. Un operador en creativo tambien (para ayudar en el servidor). */
    public boolean isAllowed(Player player) {
        if (this.owner == null) return true;
        if (this.owner.equals(player.getUUID())) return true;
        return player.isCreative() && player.hasPermissions(2);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.nutrisafio_mobs.locked_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean stillValid(Player player) {
        return isAllowed(player) && super.stillValid(player);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.ownerName = tag.getString("OwnerName");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
        if (this.owner != null) tag.putUUID("Owner", this.owner);
        tag.putString("OwnerName", this.ownerName);
    }

    // ---- Tolvas: no pueden acceder por ningun lado ----
    @Override
    public int[] getSlotsForFace(Direction side) {
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
