package com.gl.vehicles.gui;

import com.gl.vehicles.GLVehicles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class VehicleWorkbenchScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final Inventory inventory = new SimpleInventory(3); // 2 inputs, 1 output

    public VehicleWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public VehicleWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(GLVehicles.WORKBENCH_SCREEN_HANDLER, syncId);
        this.context = context;

        // Slots de fabricación
        this.addSlot(new Slot(inventory, 0, 20, 35));
        this.addSlot(new Slot(inventory, 1, 40, 35));
        this.addSlot(new Slot(inventory, 2, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) { return false; }
        });

        // Inventario Jugador
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, com.gl.vehicles.block.ModBlocks.VEHICLE_WORKBENCH);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < 3) {
                if (!this.insertItem(originalStack, 3, 39, true)) return ItemStack.EMPTY;
            } else if (!this.insertItem(originalStack, 0, 2, false)) return ItemStack.EMPTY;

            if (originalStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }
}
