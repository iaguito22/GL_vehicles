package com.gl.vehicles.item;

import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class VehicleSpawnItem extends Item {
    private final EntityType<? extends AbstractVehicleEntity> entityType;

    public VehicleSpawnItem(EntityType<? extends AbstractVehicleEntity> entityType, Settings settings) {
        super(settings);
        this.entityType = entityType;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        ItemStack itemStack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();

        BlockPos spawnPos = blockPos.offset(direction);
        
        AbstractVehicleEntity vehicle = entityType.create(world);
        if (vehicle != null) {
            vehicle.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, context.getPlayerYaw(), 0.0f);
            world.spawnEntity(vehicle);
            itemStack.decrement(1);
            return ActionResult.CONSUME;
        }

        return ActionResult.FAIL;
    }
}
