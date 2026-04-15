package com.gl.vehicles.block;

import com.gl.vehicles.GLVehicles;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.minecraft.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class ModBlocks {
    public static final Block GAS_PUMP = registerBlock("gas_pump",
            new GasPumpBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(4.0f)));

    public static final BlockEntityType<GasPumpBlockEntity> GAS_PUMP_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(GLVehicles.MOD_ID, "gas_pump_entity"),
            FabricBlockEntityTypeBuilder.create(GasPumpBlockEntity::new, GAS_PUMP).build()
    );

    public static final Block UNLOADING_GRATE = registerBlock("unloading_grate",
            new UnloadingGrateBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(3.0f).nonOpaque()));

    public static final Block VEHICLE_WORKBENCH = registerBlock("vehicle_workbench",
            new VehicleWorkbenchBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(5.0f).nonOpaque()));

    public static final Block SILO_WALL = registerBlock("silo_wall",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(3.0f)));

    public static final Block AUGER_PIPE = registerBlock("auger_pipe",
            new AugerPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(3.0f).nonOpaque()));

    private static Block registerBlock(String name, Block block) {

        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(GLVehicles.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(GLVehicles.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        GLVehicles.LOGGER.info("Registering Mod Blocks for " + GLVehicles.MOD_ID);
    }
}
