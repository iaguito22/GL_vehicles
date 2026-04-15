package com.gl.vehicles.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.gl.vehicles.GLVehicles;

public class ModItems {
    // Motores (Settings, Speed, Fuel, BasePitch, MaxPitch)
    public static final Item CHAINSAW_ENGINE = registerItem("chainsaw_engine", new EngineItem(new Item.Settings(), 0.5f, 0.2f, 0.8f, 1.8f));
    public static final Item ENGINE_1L = registerItem("engine_1l", new EngineItem(new Item.Settings(), 0.8f, 0.5f, 0.6f, 1.2f));
    public static final Item ENGINE_1_9TDI = registerItem("engine_1_9tdi", new EngineItem(new Item.Settings(), 1.0f, 0.7f, 0.4f, 1.0f, 0.1f)); 
    public static final Item ENGINE_V6 = registerItem("engine_v6", new EngineItem(new Item.Settings(), 1.5f, 1.2f, 0.5f, 1.4f));
    public static final Item ENGINE_V12 = registerItem("engine_v12", new EngineItem(new Item.Settings(), 2.5f, 2.5f, 0.7f, 2.0f));
    public static final Item ELECTRIC_MOTOR = registerItem("electric_motor", new EngineItem(new Item.Settings(), 1.4f, 0.1f, 1.0f, 1.8f));
    public static final Item WRENCH = registerItem("wrench", new WrenchItem(new Item.Settings().maxCount(1)));

    // Ruedas
    public static final Item TRACTOR_WHEELS = registerItem("tractor_wheels", new WheelItem(new Item.Settings(), 2.0f));
    public static final Item ROAD_WHEELS = registerItem("road_wheels", new WheelItem(new Item.Settings(), 1.5f));
    public static final Item SPORT_WHEELS = registerItem("sport_wheels", new WheelItem(new Item.Settings(), 3.0f));

    // Combustibles y Otros
    public static final Item FUEL_CAN = registerItem("fuel_can", new FuelCanItem(new Item.Settings().maxCount(1), 100.0f));

    // Vehículos
    public static final Item TRACTOR = registerItem("tractor", new VehicleSpawnItem(com.gl.vehicles.entity.ModEntities.TRACTOR, new Item.Settings().maxCount(1)));
    public static final Item KART = registerItem("kart", new VehicleSpawnItem(com.gl.vehicles.entity.ModEntities.KART, new Item.Settings().maxCount(1)));

    // Accesorios (Attachments)
    public static final Item HARVESTER = registerItem("harvester", new AttachmentItem(new Item.Settings().maxCount(1), AttachmentItem.AttachmentType.FRONT));
    public static final Item TRAILER = registerItem("trailer", new AttachmentItem(new Item.Settings().maxCount(1), AttachmentItem.AttachmentType.REAR));
    public static final Item SEEDER = registerItem("seeder", new AttachmentItem(new Item.Settings().maxCount(1), AttachmentItem.AttachmentType.REAR));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(GLVehicles.MOD_ID, name), item);
    }

    public static void registerModItems() {
        GLVehicles.LOGGER.info("Registering Mod Items for " + GLVehicles.MOD_ID);
    }
}
