package com.gl.vehicles.gui;

import com.gl.vehicles.GLVehicles;
import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class VehicleScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final AbstractVehicleEntity vehicle;

    // Cliente
    public VehicleScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(buf.readInt()),
                (AbstractVehicleEntity) playerInventory.player.getWorld().getEntityById(buf.readInt()));
    }

    // Servidor
    public VehicleScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
            AbstractVehicleEntity vehicle) {
        super(GLVehicles.VEHICLE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.vehicle = vehicle;
        inventory.onOpen(playerInventory.player);

        if (vehicle instanceof com.gl.vehicles.entity.KartEntity) {
            // Kart: Solo 2 slots (Motor, Ruedas)
            this.addSlot(new Slot(inventory, 0, 8, 18) {
                @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof com.gl.vehicles.item.EngineItem; }
            });
            this.addSlot(new Slot(inventory, 1, 8, 36) {
                @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof com.gl.vehicles.item.WheelItem; }
            });
        } else if (vehicle instanceof com.gl.vehicles.entity.TractorEntity) {
            // Tractor: 4 slots (Frontal, Motor, Ruedas, Trasero)
            for (int i = 0; i < 4; i++) {
                final int slotIndex = i;
                this.addSlot(new Slot(inventory, i, 8, 18 + (i * 18)) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        if (slotIndex == 1) return stack.getItem() instanceof com.gl.vehicles.item.EngineItem;
                        if (slotIndex == 2) return stack.getItem() instanceof com.gl.vehicles.item.WheelItem;
                        return super.canInsert(stack);
                    }
                });
            }

            // Slots de inventario del remolque (Solo para tractores)
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 9; ++j) {
                    int slotIndex = 4 + j + i * 9;
                    if (slotIndex < inventory.size()) {
                        this.addSlot(new Slot(inventory, slotIndex, 8 + j * 18, 90 + i * 18));
                    }
                }
            }
        }

        // Inventario Jugador
        int startY = (vehicle instanceof com.gl.vehicles.entity.KartEntity) ? 103 : 157; 
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, startY + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, startY + 58));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (vehicle != null) {
            vehicle.calculateStats();
        }
    }

    public AbstractVehicleEntity getVehicle() {
        return vehicle;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false))
                return ItemStack.EMPTY;
            if (originalStack.isEmpty())
                slot.setStack(ItemStack.EMPTY);
            else
                slot.markDirty();
        }
        return newStack;
    }
}
