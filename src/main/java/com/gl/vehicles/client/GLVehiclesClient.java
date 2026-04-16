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
                
                // Etiqueta FUEL y Porcentaje
                String fuelLabel = "FUEL " + String.format("%.0f%%", fuel);
                drawContext.drawTextWithShadow(client.textRenderer, fuelLabel, x - 25, y - 12, 0xFFFFAA00);

                // Nivel de fuel vertical (Naranja/Rojo según nivel)
                int fuelColor = fuel > 25 ? 0xFFFFAA00 : 0xFFFF0000;
                int scaledFuelHeight = (int)((fuel / 100.0f) * barHeight);
                drawContext.fill(x, y + barHeight - scaledFuelHeight, x + barWidth, y + barHeight, fuelColor);
                
                // --- HUD DE VEHÍCULO PULIDO (REDONDEADO Y CLARO) ---
                int centerX = client.getWindow().getScaledWidth() / 2;
                int hudY = client.getWindow().getScaledHeight() - 75;
                
                // Fondo semi-transparente muy redondeado (Opacidad suave para evitar oscurecimiento)
                int shadowColor = 0x22000000; // Muy ligero, al sumarse queda perfecto
                drawContext.fill(centerX - 64, hudY - 21, centerX + 64, hudY + 31, shadowColor); 
                drawContext.fill(centerX - 62, hudY - 23, centerX + 62, hudY + 33, shadowColor); 
                drawContext.fill(centerX - 58, hudY - 25, centerX + 58, hudY + 35, shadowColor); 
                drawContext.fill(centerX - 50, hudY - 27, centerX + 50, hudY + 37, shadowColor); 
                
                // Línea superior decorativa (Cian brillante)
                drawContext.fill(centerX - 50, hudY - 27, centerX + 50, hudY - 25, 0xAA55FFFF); 

                // Velocidad (Grande y Central)
                double speed = vehicle.getForwardSpeed() * 72.0;
                String speedText = String.format("%.0f", speed);
                drawContext.drawTextWithShadow(client.textRenderer, speedText, centerX - (client.textRenderer.getWidth(speedText) / 2), hudY - 10, 0xFFFFFFFF);
                drawContext.drawTextWithShadow(client.textRenderer, "km/h", centerX - (client.textRenderer.getWidth("km/h") / 2), hudY + 2, 0xFF55FFFF);

                // Marcha (Sincronizada con el Servidor - 5 marchas + R)
                int gear = vehicle.getGear();
                String gearText = gear == 0 ? "P" : (gear == -1 ? "R" : String.valueOf(gear));
                
                drawContext.drawTextWithShadow(client.textRenderer, "GEAR", centerX + 30, hudY - 15, 0xFFBBBBBB);
                drawContext.drawTextWithShadow(client.textRenderer, gearText, centerX + 35, hudY - 5, 0xFFFFAA00);

                // --- BARRA DE RPM CONTINUA (Vibrante) ---
                int rpmX = centerX - 50;
                int rpmBottomY = hudY + 20;
                float rpmPercent = vehicle.getRpm();
                
                // Color dinámico de la barra según RPM
                int currentRpmColor = 0xFF00FF00; // Verde base
                if (rpmPercent > 0.7f) currentRpmColor = 0xFFFFAA00;
                if (rpmPercent > 0.9f) currentRpmColor = 0xFFFF0000;

                // Fondo de la barra RPM
                drawContext.fill(rpmX, rpmBottomY, rpmX + 100, rpmBottomY + 4, 0x44000000);
                
                // Dibujar gradiente / barra continua
                for(int i = 0; i < (int)(rpmPercent * 100); i++) {
                    int blockColor = 0xFF00FF00;
                    if (i > 70) blockColor = 0xFFFFAA00;
                    if (i > 90) blockColor = 0xFFFF0000;
                    drawContext.fill(rpmX + i, rpmBottomY, rpmX + i + 1, rpmBottomY + 4, blockColor);
                }
                
                // Efecto de parpadeo del limitador (más suave)
                if (rpmPercent > 0.96f && client.world.getTime() % 2 == 0) {
                    drawContext.fill(centerX - 62, hudY - 23, centerX + 62, hudY + 33, 0x22FF0000);
                }

                // --- CARGA DEL REMOLQUE (Sincronizado desde el Servidor) ---
                if (vehicle.getInventory().size() > 4) {
                    int occupiedSlots = vehicle.getDataTracker().get(AbstractVehicleEntity.OCCUPIED_SLOTS);
                    int totalCargoSlots = vehicle.getInventory().size() - 4;

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

        // WASD Input Prediction
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.getVehicle() instanceof AbstractVehicleEntity vehicle) {
                boolean f = client.options.forwardKey.isPressed();
                boolean b = client.options.backKey.isPressed();
                boolean l = client.options.leftKey.isPressed();
                boolean r = client.options.rightKey.isPressed();
                boolean j = client.options.jumpKey.isPressed();
                
                // Aplicar localmente para que handlePhysics() en el cliente tenga los inputs actualizados
                vehicle.setInputs(f, b, l, r, j);
                
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(f);
                buf.writeBoolean(b);
                buf.writeBoolean(l);
                buf.writeBoolean(r);
                buf.writeBoolean(j);
                ClientPlayNetworking.send(GLVehicles.INPUT_PACKET_ID, buf);
            }
        });
    }
}
