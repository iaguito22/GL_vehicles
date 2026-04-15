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

                // Caso A: El bidón tiene fuel y el surtidor tiene espacio -> Llenar surtidor
                if (currentFuelInCan > 0 && currentFuelInPump < pump.getMaxCapacity()) {
                    float amountToTransfer = Math.min(currentFuelInCan, 20.0f);
                    pump.addFuel(amountToTransfer);
                    can.setFuel(stack, currentFuelInCan - amountToTransfer);
                    player.sendMessage(Text.literal("⬇️ Llenando Surtidor... (" + String.format("%.0f", pump.getFuel()) + "L)").formatted(Formatting.YELLOW), true);
                } 
                // Caso B: El surtidor tiene fuel y el bidón tiene espacio -> Llenar bidón
                else if (currentFuelInPump > 0 && currentFuelInCan < can.getCapacity()) {
                    float amountToTransfer = Math.min(currentFuelInPump, 20.0f);
                    pump.addFuel(-amountToTransfer);
                    can.setFuel(stack, currentFuelInCan + amountToTransfer);
                    player.sendMessage(Text.literal("⬆️ Llenando Bidón... (" + String.format("%.0f", can.getFuel(stack)) + "L)").formatted(Formatting.GREEN), true);
                }
            }
            return ActionResult.SUCCESS;
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
