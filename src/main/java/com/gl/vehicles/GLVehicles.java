package com.gl.vehicles;

import com.gl.vehicles.entity.ModEntities;
import com.gl.vehicles.gui.VehicleScreenHandler;
import com.gl.vehicles.item.ModItems;
import com.gl.vehicles.sound.ModSounds;
import com.gl.vehicles.network.VehicleInputC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GLVehicles implements ModInitializer {
    public static final String MOD_ID = "gl_vehicles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier INPUT_PACKET_ID = new Identifier(MOD_ID, "vehicle_input");

    public static final ScreenHandlerType<VehicleScreenHandler> VEHICLE_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    new Identifier(MOD_ID, "vehicle_screen"),
                    new ExtendedScreenHandlerType<>(VehicleScreenHandler::new)
            );

    public static final ScreenHandlerType<com.gl.vehicles.gui.VehicleWorkbenchScreenHandler> WORKBENCH_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    new Identifier(MOD_ID, "vehicle_workbench"),
                    new ScreenHandlerType<>(com.gl.vehicles.gui.VehicleWorkbenchScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES)
            );

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing GL Vehicles mod...");

        com.gl.vehicles.item.ModItemGroups.registerItemGroups();
        com.gl.vehicles.block.ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModEntities.registerEntities();
        ModSounds.registerSounds();

        // Register Network Packet
        ServerPlayNetworking.registerGlobalReceiver(INPUT_PACKET_ID, VehicleInputC2SPacket::handle);

        // Force the VEHICLE_SCREEN_HANDLER field to initialise (already done by static init,
        // but referencing it here makes the registration order explicit and avoids issues with
        // lazy class-loading depending on JVM implementation).
        LOGGER.info("Registered ScreenHandlerType: {}", VEHICLE_SCREEN_HANDLER);
    }
}
