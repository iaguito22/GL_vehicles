package com.gl.vehicles.network;

import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class VehicleInputC2SPacket {
    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean f = buf.readBoolean();
        boolean b = buf.readBoolean();
        boolean l = buf.readBoolean();
        boolean r = buf.readBoolean();
        boolean j = buf.readBoolean();

        server.execute(() -> {
            if (player.getVehicle() instanceof AbstractVehicleEntity vehicle) {
                vehicle.setInputs(f, b, l, r, j);
            }
        });
    }
}
