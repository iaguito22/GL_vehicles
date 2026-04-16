package com.gl.vehicles.block;

import com.gl.vehicles.entity.AbstractVehicleEntity;
import com.gl.vehicles.item.ModItems;
import com.gl.vehicles.item.FuelCanItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;

public class GasPumpBlock extends Block implements BlockEntityProvider {
    public GasPumpBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GasPumpBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof GasPumpBlockEntity pump)) return ActionResult.PASS;

        // Mostrar estado al usar con la mano vacía
        if (stack.isEmpty() && !world.isClient) {
            player.sendMessage(Text.literal("⛽ Surtidor: " + String.format("%.1f", pump.getFuel()) + " / " + pump.getMaxCapacity() + " L").formatted(Formatting.AQUA), true);
            return ActionResult.SUCCESS;
        }

        // Rellenar el Surtidor con un Bidón O Llenar el Bidón desde el Surtidor
        if (stack.getItem() instanceof FuelCanItem can) {
            if (!world.isClient) {
                float currentFuelInCan = can.getFuel(stack);
                float currentFuelInPump = pump.getFuel();
                
                if (player.isSneaking()) { // Surtidor -> Bidón
                    if (currentFuelInPump > 0 && currentFuelInCan < 50.0f) {
                        float amount = Math.min(currentFuelInPump, 50.0f - currentFuelInCan);
                        pump.addFuel(-amount);
                        can.setFuel(stack, currentFuelInCan + amount);
                        player.sendMessage(Text.literal("⬆️ Rellenando Bidón (+50L)").formatted(Formatting.GREEN), true);
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.ITEM_BUCKET_FILL, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                } else { // Bidón -> Surtidor
                    if (currentFuelInCan > 0 && currentFuelInPump < pump.getMaxCapacity()) {
                        float amount = Math.min(currentFuelInCan, 50.0f);
                        pump.addFuel(amount);
                        can.setFuel(stack, currentFuelInCan - amount);
                        player.sendMessage(Text.literal("⬇️ Llenando Depósito (+50L)").formatted(Formatting.YELLOW), true);
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.ITEM_BUCKET_EMPTY, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }

        // Rellenar un Vehículo cercano (Radio 4 bloques)
        if (!world.isClient) {
            if (pump.getFuel() <= 0) {
                player.sendMessage(Text.literal("¡SURTIDOR VACÍO!").formatted(Formatting.RED), true);
                return ActionResult.SUCCESS;
            }

            List<AbstractVehicleEntity> vehicles = world.getEntitiesByClass(
                AbstractVehicleEntity.class,
                new Box(pos).expand(4.0),
                e -> e.getFuel() < 100.0f
            );
            
            if (!vehicles.isEmpty()) {
                AbstractVehicleEntity vehicle = vehicles.get(0);
                vehicle.setFuel(vehicle.getFuel() + 5.0f);
                pump.addFuel(-5.0f); // Gastar del surtidor
                player.sendMessage(Text.literal("Refueling... " + String.format("%.0f%%", vehicle.getFuel())).formatted(Formatting.GOLD), true);
                return ActionResult.SUCCESS;
            }
        }
        
        return ActionResult.CONSUME;
    }
}
