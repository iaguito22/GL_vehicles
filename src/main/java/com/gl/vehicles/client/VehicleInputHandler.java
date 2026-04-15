package com.gl.vehicles.client;

import com.gl.vehicles.GLVehicles;
import com.gl.vehicles.entity.AbstractVehicleEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.PacketByteBuf;

/**
 * Runs every client tick while the player is riding a vehicle.
 * Reads WASD key state and sends it to the server via a network packet.
 */
public class VehicleInputHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(VehicleInputHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null) return;
        if (!(client.player.getVehicle() instanceof AbstractVehicleEntity)) return;

        GameOptions options = client.options;
        boolean forward  = options.forwardKey.isPressed();
        boolean backward = options.backKey.isPressed();
        boolean left     = options.leftKey.isPressed();
        boolean right    = options.rightKey.isPressed();
        boolean jump     = options.jumpKey.isPressed();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(forward);
        buf.writeBoolean(backward);
        buf.writeBoolean(left);
        buf.writeBoolean(right);
        buf.writeBoolean(jump);

        ClientPlayNetworking.send(GLVehicles.INPUT_PACKET_ID, buf);
    }
}
