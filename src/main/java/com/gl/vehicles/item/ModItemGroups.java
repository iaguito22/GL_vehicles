package com.gl.vehicles.item;

import com.gl.vehicles.GLVehicles;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup VEHICLES_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(GLVehicles.MOD_ID, "vehicles"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemgroup.gl_vehicles.vehicles"))
                    .icon(() -> new ItemStack(ModItems.WRENCH))
                    .entries((displayContext, entries) -> {
                        entries.add(com.gl.vehicles.block.ModBlocks.GAS_PUMP);
                        entries.add(com.gl.vehicles.block.ModBlocks.UNLOADING_GRATE);
                        entries.add(ModItems.TRACTOR);
                        entries.add(ModItems.KART);
                        entries.add(ModItems.HARVESTER);
                        entries.add(ModItems.TRAILER);
                        entries.add(ModItems.SEEDER);
                        entries.add(ModItems.WRENCH);
                        entries.add(ModItems.CHAINSAW_ENGINE);
                        entries.add(ModItems.ENGINE_1L);
                        entries.add(ModItems.ENGINE_1_9TDI);
                        entries.add(ModItems.ENGINE_V6);
                        entries.add(ModItems.ENGINE_V12);
                        entries.add(ModItems.ELECTRIC_MOTOR);
                        entries.add(ModItems.TRACTOR_WHEELS);
                        entries.add(ModItems.ROAD_WHEELS);
                        entries.add(ModItems.SPORT_WHEELS);
                        entries.add(ModItems.FUEL_CAN);
                    })
                    .build());

    public static void registerItemGroups() {
        GLVehicles.LOGGER.info("Registering Item Groups for " + GLVehicles.MOD_ID);
    }
}
