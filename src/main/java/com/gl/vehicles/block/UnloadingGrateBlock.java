package com.gl.vehicles.block;

import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventories;

public class UnloadingGrateBlock extends Block {
    public UnloadingGrateBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof AbstractVehicleEntity vehicle) {
            // Only unload if the vehicle is moving slowly or stopped on top
            if (vehicle.getVelocity().horizontalLengthSquared() < 0.01) {
                unloadVehicle(world, pos, vehicle);
            }
        }
    }

    private void unloadVehicle(World world, BlockPos pos, AbstractVehicleEntity vehicle) {
        Inventory vehicleInv = vehicle.getInventory();
        BlockEntity targetBE = world.getBlockEntity(pos.down());
        
        if (targetBE instanceof Inventory targetInv) {
            for (int i = 0; i < vehicleInv.size(); i++) {
                // Skip engine and wheels slots
                if (vehicle instanceof com.gl.vehicles.entity.TractorEntity && (i == 1 || i == 2)) continue;
                if (vehicle instanceof com.gl.vehicles.entity.KartEntity && (i == 0 || i == 1)) continue;

                ItemStack stack = vehicleInv.getStack(i);
                if (!stack.isEmpty()) {
                    ItemStack remainder = addItemToInventory(targetInv, stack);
                    vehicleInv.setStack(i, remainder);
                    if (remainder.isEmpty()) {
                        vehicle.calculateStats(); // Recalculate if weight changed
                        break; // Only unload one slot per collision tick to be smooth
                    }
                }
            }
        }
    }

    private ItemStack addItemToInventory(Inventory inv, ItemStack stack) {
        ItemStack result = stack.copy();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack invStack = inv.getStack(i);
            if (invStack.isEmpty()) {
                inv.setStack(i, result);
                return ItemStack.EMPTY;
            } else if (ItemStack.canCombine(invStack, result)) {
                int count = Math.min(invStack.getMaxCount() - invStack.getCount(), result.getCount());
                invStack.increment(count);
                result.decrement(count);
                if (result.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return result;
    }
}
