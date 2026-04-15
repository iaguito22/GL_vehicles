package com.gl.vehicles.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import com.gl.vehicles.block.ModBlocks;

public class GasPumpBlockEntity extends BlockEntity {
    private float fuel = 0.0f;
    private final float maxCapacity = 1000.0f; // Capacidad industrial

    public GasPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GAS_PUMP_ENTITY, pos, state);
    }

    public float getFuel() { return fuel; }
    public float getMaxCapacity() { return maxCapacity; }
    
    public void setFuel(float amount) {
        this.fuel = Math.max(0, Math.min(maxCapacity, amount));
        markDirty();
    }

    public void addFuel(float amount) {
        setFuel(this.fuel + amount);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.fuel = nbt.getFloat("Fuel");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putFloat("Fuel", this.fuel);
    }

    // --- SINCRONIZACIÓN DE RED ---
    @Override
    public net.minecraft.network.packet.Packet<net.minecraft.network.listener.ClientPlayPacketListener> toUpdatePacket() {
        return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
