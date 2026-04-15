package com.gl.vehicles.client;

import com.gl.vehicles.GLVehicles;
import com.gl.vehicles.client.gui.VehicleScreen;
import com.gl.vehicles.client.model.TractorEntityModel;
import com.gl.vehicles.client.render.TractorEntityRenderer;
import com.gl.vehicles.entity.AbstractVehicleEntity;
import com.gl.vehicles.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Inventory;

public class GLVehiclesClient implements ClientModInitializer {
    public static final EntityModelLayer TRACTOR_MODEL_LAYER = new EntityModelLayer(new Identifier(GLVehicles.MOD_ID, "tractor"), "main");

    @Override
    public void onInitializeClient() {
        HandledScreens.register(GLVehicles.VEHICLE_SCREEN_HANDLER, VehicleScreen::new);
        // HandledScreens.register(GLVehicles.WORKBENCH_SCREEN_HANDLER, (handler, inv, title) -> null); // Placeholder for now
        EntityRendererRegistry.register(ModEntities.TRACTOR, TractorEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.KART, com.gl.vehicles.client.render.KartRenderer::new);

        // HUD Overlay para la Gasolina (Barra Vertical a la Derecha)
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client.player != null && client.player.getVehicle() instanceof AbstractVehicleEntity vehicle) {
                float fuel = vehicle.getFuel();
                int barHeight = 60;
                int barWidth = 8;
                int x = client.getWindow().getScaledWidth() - 30; // Margen derecha
                int y = client.getWindow().getScaledHeight() - 20 - barHeight; // Margen abajo
                
                // Fondo de la barra (Gris)
                drawContext.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
                
                // Etiqueta FUEL
                drawContext.drawTextWithShadow(client.textRenderer, "FUEL", x - 10, y - 10, 0xFFFFAA00);

                // Nivel de fuel vertical (Naranja/Rojo según nivel)
                int fuelColor = fuel > 25 ? 0xFFFFAA00 : 0xFFFF0000;
                int scaledFuelHeight = (int)((fuel / 100.0f) * barHeight);
                drawContext.fill(x, y + barHeight - scaledFuelHeight, x + barWidth, y + barHeight, fuelColor);
                
                // --- HUD DE VEHÍCULO PULIDO ---
                int centerX = client.getWindow().getScaledWidth() / 2;
                int hudY = client.getWindow().getScaledHeight() - 75;
                
                // Fondo semi-transparente "redondeado" con capas
                drawContext.fill(centerX - 62, hudY - 23, centerX + 62, hudY + 33, 0x88000000); // Principal
                drawContext.fill(centerX - 60, hudY - 25, centerX + 60, hudY + 35, 0x88000000); // Offset
                drawContext.fill(centerX - 60, hudY - 27, centerX + 60, hudY - 25, 0xAA55FFFF); // Línea superior decorativa

                // Velocidad (Grande y Central)
                double speed = vehicle.getVelocity().horizontalLength() * 72.0;
                String speedText = String.format("%.0f", speed);
                drawContext.drawTextWithShadow(client.textRenderer, speedText, centerX - (client.textRenderer.getWidth(speedText) / 2), hudY - 10, 0xFFFFFFFF);
                drawContext.drawTextWithShadow(client.textRenderer, "km/h", centerX - (client.textRenderer.getWidth("km/h") / 2), hudY + 2, 0xFF55FFFF);

                // Marcha (Sincronizada con el Servidor - 5 marchas)
                int gear = vehicle.getDataTracker().get(AbstractVehicleEntity.GEAR);
                String gearText = gear == 0 ? "P" : String.valueOf(gear);
                
                drawContext.drawTextWithShadow(client.textRenderer, "GEAR", centerX + 30, hudY - 15, 0xFFBBBBBB);
                drawContext.drawTextWithShadow(client.textRenderer, gearText, centerX + 35, hudY - 5, 0xFFFFAA00);

                // --- BARRA DE RPM CONTINUA (Inferior) ---
                int rpmX = centerX - 50;
                int rpmBottomY = hudY + 20;
                float rpmPercent = vehicle.getDataTracker().get(AbstractVehicleEntity.RPM_SYNC);

                // Fondo de la barra RPM
                drawContext.fill(rpmX, rpmBottomY, rpmX + 100, rpmBottomY + 4, 0x44000000);
                
                // Dibujar gradiente de color (Verde -> Naranja -> Rojo)
                for(int i = 0; i < (int)(rpmPercent * 100); i++) {
                    int blockColor = 0xFF00FF00; // Verde
                    if (i > 70) blockColor = 0xFFFFAA00; // Naranja
                    if (i > 90) blockColor = 0xFFFF0000; // Rojo
                    drawContext.fill(rpmX + i, rpmBottomY, rpmX + i + 1, rpmBottomY + 4, blockColor);
                }
                
                // Efecto de parpadeo del limitador
                if (rpmPercent > 0.95f && client.world.getTime() % 2 == 0) {
                    drawContext.fill(centerX - 60, hudY - 25, centerX + 60, hudY + 35, 0x44FF0000);
                }

                // --- CARGA DEL REMOLQUE (Simplificado para Inventario Extendido) ---
                if (vehicle.getInventory().size() > 4) {
                    int occupiedSlots = 0;
                    Inventory inv = vehicle.getInventory();
                    int totalCargoSlots = inv.size() - 4;
                    for (int i = 4; i < inv.size(); i++) if (!inv.getStack(i).isEmpty()) occupiedSlots++;
                    
                    int cargoYOffset = y;
                    drawContext.fill(20, cargoYOffset + barHeight - 40, 80, cargoYOffset + barHeight + 5, 0x88000000);
                    drawContext.drawTextWithShadow(client.textRenderer, "CARGO", 25, cargoYOffset + barHeight - 35, 0xFFBBBBBB);
                    String cargoInfo = occupiedSlots + "/" + totalCargoSlots;
                    drawContext.drawTextWithShadow(client.textRenderer, cargoInfo, 25, cargoYOffset + barHeight - 25, 0xFF00AAFF);
                    
                    // Mini barra de carga
                    int barLen = (int)((occupiedSlots / (float)totalCargoSlots) * 50);
                    drawContext.fill(25, cargoYOffset + barHeight - 15, 25 + 50, cargoYOffset + barHeight - 12, 0x44FFFFFF);
                    drawContext.fill(25, cargoYOffset + barHeight - 15, 25 + barLen, cargoYOffset + barHeight - 12, 0xFF00AAFF);
                }
            }
        });

        // WASD Input
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.getVehicle() instanceof AbstractVehicleEntity) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(client.options.forwardKey.isPressed());
                buf.writeBoolean(client.options.backKey.isPressed());
                buf.writeBoolean(client.options.leftKey.isPressed());
                buf.writeBoolean(client.options.rightKey.isPressed());
                ClientPlayNetworking.send(GLVehicles.INPUT_PACKET_ID, buf);
            }
        });
    }
}
