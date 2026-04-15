package com.gl.vehicles.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.inventory.Inventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;

public class AugerPipeBlock extends FacingBlock {
    public static final DirectionProperty FACING = FacingBlock.FACING;

    public AugerPipeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide());
    }

    @Override
    public void scheduledTick(BlockState state, net.minecraft.server.world.ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        moveItems(state, world, pos);
        world.scheduleBlockTick(pos, this, 8); // Tick cada 8 game ticks (rápido)
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 8);
    }

    private void moveItems(BlockState state, World world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos sourcePos = pos.offset(facing.getOpposite());
        BlockPos targetPos = pos.offset(facing);

        BlockEntity sourceBE = world.getBlockEntity(sourcePos);
        BlockEntity targetBE = world.getBlockEntity(targetPos);

        if (sourceBE instanceof Inventory sourceInv && targetBE instanceof Inventory targetInv) {
            for (int i = 0; i < sourceInv.size(); i++) {
                ItemStack stack = sourceInv.getStack(i);
                if (!stack.isEmpty()) {
                    ItemStack remainder = addItemToInventory(targetInv, stack.split(1));
                    if (!remainder.isEmpty()) {
                        stack.increment(1); // Devolver si no cupo
                    } else {
                        sourceInv.markDirty();
                        targetInv.markDirty();
                        break; // Mover 1 ítem por tick
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
