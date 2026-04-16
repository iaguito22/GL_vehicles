package com.gl.vehicles.client.sound;

import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public class VehicleEngineSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private final AbstractVehicleEntity vehicle;
    private boolean done = false;

    public VehicleEngineSoundInstance(AbstractVehicleEntity vehicle, SoundEvent soundEvent) {
        // Usamos BLOCKS ya que el usuario confirmó que con el comando en esa categoría
        // funciona
        super(soundEvent, SoundCategory.BLOCKS, SoundInstance.createRandom());
        this.vehicle = vehicle;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.01f; // Empezamos muy bajo para el fade-in
        this.pitch = 1.0f;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    protected final void setDone() {
        this.done = true;
        this.repeat = false;
    }

    @Override
    public void tick() {
        if (this.vehicle.isRemoved() || this.vehicle.isDestroyed() || !this.vehicle.isAlive()) {
            this.setDone();
            return;
        }

        // REQUISITO: El sonido muere si se apaga el coche (Gear 0), se queda sin fuel o se destruye
        boolean gearEngaged = vehicle.getGear() != 0;
        boolean hasFuel = vehicle.getFuel() > 0;

        if (!hasFuel || !gearEngaged) {
            // Fade out suave antes de apagar el sonido
            this.volume = MathHelper.lerp(0.2f, this.volume, 0.0f);
            if (this.volume < 0.01f) {
                this.setDone();
            }
            return;
        }

        this.x = (float) this.vehicle.getX();
        this.y = (float) this.vehicle.getY();
        this.z = (float) this.vehicle.getZ();

        // Volumen dinámico basado en el motor
        float baseVol = vehicle.getEngineBaseVolume();
        float targetVolume = vehicle.hasPassengers() ? baseVol : baseVol * 0.5f;
        this.volume = MathHelper.lerp(0.15f, this.volume, targetVolume);

        // Pitch dinámico basado en las estadísticas del motor
        float basePitch = vehicle.getEngineBasePitch();
        float maxPitch = vehicle.getEngineMaxPitch();
        float rpm = vehicle.getRpm();
        float targetPitch = basePitch + (rpm * (maxPitch - basePitch));
        this.pitch = MathHelper.lerp(0.25f, this.pitch, targetPitch);
    }
}
