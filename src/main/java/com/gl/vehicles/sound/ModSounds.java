package com.gl.vehicles.sound;

import com.gl.vehicles.GLVehicles;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent CHAINSAW_SOUND = registerSound("chainsaw_idle");
    public static final SoundEvent TDI_SOUND = registerSound("tdi_idle");
    public static final SoundEvent V12_SOUND = registerSound("v12_idle");
    public static final SoundEvent V6_SOUND = registerSound("v6_idle");
    public static final SoundEvent ENGINE_1L_SOUND = registerSound("engine_1l_idle");
    public static final SoundEvent ELECTRIC_WHINE = registerSound("electric_whine");

    private static SoundEvent registerSound(String name) {
        Identifier id = new Identifier(GLVehicles.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        GLVehicles.LOGGER.info("Registering Sounds for " + GLVehicles.MOD_ID);
    }
}
