package com.gl.vehicles.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TractorEntity extends AbstractVehicleEntity {



    public TractorEntity(EntityType<? extends TractorEntity> type, World world) {
        super(type, world, 31); // 4 básicos + 27 para remolque/sembradora
    }



    @Override
    public ItemStack getEngineStack() {
        return this.inventory.getStack(1); // Slot 1: Motor
    }

    @Override
    public ItemStack getWheelStack() {
        return this.inventory.getStack(2); // Slot 2: Ruedas (representativo)
    }

    @Override
    public float getBaseWeight() {
        return 150.0f; // Los tractores son pesados
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.53D; // Elevado 3 píxeles respecto al base (0.34 + 0.19)
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        if (Math.abs(forwardSpeed) > 0.05) {
            ItemStack harvester = this.inventory.getStack(0);
            if (harvester.isOf(com.gl.vehicles.item.ModItems.HARVESTER)) {
                net.minecraft.util.math.Vec3d forward = net.minecraft.util.math.Vec3d.fromPolar(0, this.getYaw()).normalize();
                net.minecraft.util.math.Vec3d right = forward.crossProduct(new net.minecraft.util.math.Vec3d(0, 1, 0));
                
                for (int i = -1; i <= 1; i++) {
                    net.minecraft.util.math.BlockPos checkPos = net.minecraft.util.math.BlockPos.ofFloored(this.getPos().add(forward.multiply(2.0)).add(right.multiply(i)));
                    net.minecraft.block.BlockState state = this.getWorld().getBlockState(checkPos);
                    
                    if (state.getBlock() instanceof net.minecraft.block.CropBlock crop && crop.isMature(state)) {
                        java.util.List<ItemStack> drops = net.minecraft.block.Block.getDroppedStacks(state, (net.minecraft.server.world.ServerWorld) this.getWorld(), checkPos, null);
                        this.getWorld().breakBlock(checkPos, false, this);
                        
                        for (ItemStack drop : drops) {
                            for (int j = 4; j < 31; j++) {
                                if (drop.isEmpty()) break;
                                ItemStack slotStack = this.inventory.getStack(j);
                                if (slotStack.isEmpty()) {
                                    this.inventory.setStack(j, drop.copy());
                                    drop.setCount(0);
                                } else if (ItemStack.canCombine(slotStack, drop) && slotStack.getCount() < slotStack.getMaxCount()) {
                                    int space = slotStack.getMaxCount() - slotStack.getCount();
                                    int amount = Math.min(space, drop.getCount());
                                    slotStack.increment(amount);
                                    drop.decrement(amount);
                                }
                            }
                            if (!drop.isEmpty()) {
                                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(this.getWorld(), checkPos.getX(), checkPos.getY(), checkPos.getZ(), drop);
                                this.getWorld().spawnEntity(itemEntity);
                            }
                        }
                    }
                }
            }

            ItemStack rearAcc = this.inventory.getStack(3);
            if (rearAcc.isOf(com.gl.vehicles.item.ModItems.SEEDER)) {
                net.minecraft.util.math.BlockPos plantPos = this.getBlockPos();
                net.minecraft.util.math.BlockPos belowPos = plantPos.down();
                
                net.minecraft.block.BlockState stateBelow = this.getWorld().getBlockState(belowPos);
                if (stateBelow.isOf(net.minecraft.block.Blocks.FARMLAND) && this.getWorld().isAir(plantPos)) {
                    for (int j = 4; j < 31; j++) {
                        ItemStack slotStack = this.inventory.getStack(j);
                        if (slotStack.getItem() instanceof net.minecraft.item.AliasedBlockItem seedItem) {
                            this.getWorld().setBlockState(plantPos, seedItem.getBlock().getDefaultState());
                            slotStack.decrement(1);
                            break;
                        }
                    }
                }
            }
        }
    }
}
