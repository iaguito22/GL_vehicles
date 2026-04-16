package com.gl.vehicles.client.gui;

import com.gl.vehicles.gui.VehicleScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class VehicleScreen extends HandledScreen<VehicleScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    public VehicleScreen(VehicleScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 176;

        com.gl.vehicles.entity.AbstractVehicleEntity vehicle = handler.getVehicle();
        if (vehicle instanceof com.gl.vehicles.entity.KartEntity) {
            this.backgroundHeight = 185; // Header (89) + Inventory (96)
        } else {
            this.backgroundHeight = 239; // Header (89) + Trailer (54) + Inventory (96)
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        com.gl.vehicles.entity.AbstractVehicleEntity vehicle = this.handler.getVehicle();

        // El top (89px) es común para ambos, solo cambian los slots habilitados
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, 89);

        if (vehicle instanceof com.gl.vehicles.entity.KartEntity) {
            // Parche gris total para la zona derecha y los slots 3-4 del Kart
            // Cubrimos desde el borde de los slots (x+7) hasta el final (x+169)
            // Pero solo de la mitad hacia abajo para no tapar los slots 1-2
            context.fill(x + 7, y + 53, x + 169, y + 89, 0xFFC6C6C6);
            // Parche para la zona superior derecha (donde están las stats)
            context.fill(x + 26, y + 17, x + 169, y + 53, 0xFFC6C6C6);

            // Inventario Jugador (Directamente debajo del header)
            context.drawTexture(TEXTURE, x, y + 89, 0, 126, this.backgroundWidth, 96);
        } else {
            // Parche gris para stats en Tractor
            context.fill(x + 26, y + 17, x + 169, y + 89, 0xFFC6C6C6);
            // Inventario remolque
            context.drawTexture(TEXTURE, x, y + 89, 0, 17, this.backgroundWidth, 54);
            // Inventario Jugador
            context.drawTexture(TEXTURE, x, y + 143, 0, 126, this.backgroundWidth, 96);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        com.gl.vehicles.entity.AbstractVehicleEntity vehicle = this.handler.getVehicle();
        String titleText = (vehicle instanceof com.gl.vehicles.entity.KartEntity) ? "Kart" : "Tractor";
        context.drawText(this.textRenderer, titleText, 8, 6, 0x404040, false);

        if (vehicle != null) {
            int textColor = 0x404040;

            if (vehicle instanceof com.gl.vehicles.entity.KartEntity) {
                context.drawText(this.textRenderer, Text.literal("Motor"), 28, 22, textColor, false);
                context.drawText(this.textRenderer, Text.literal("Ruedas"), 28, 40, textColor, false);
            } else {
                context.drawText(this.textRenderer, Text.literal("Acc. Frontal"), 28, 22, textColor, false);
                context.drawText(this.textRenderer, Text.literal("Motor"), 28, 40, textColor, false);
                context.drawText(this.textRenderer, Text.literal("Ruedas"), 28, 58, textColor, false);
                context.drawText(this.textRenderer, Text.literal("Acc. Trasero"), 28, 76, textColor, false);
            }

            // --- SECCIÓN DE ESTADÍSTICAS (PERFORMANCE) ---
            int statX = 110;
            // Para el tractor subimos el inicio de las stats a 30 para dejar hueco al
            // remolque abajo
            int statY = (vehicle instanceof com.gl.vehicles.entity.KartEntity) ? 38 : 30;
            int barWidth = 56;
            int barHeight = 4;

            // --- 1. Max Speed ---
            float maxSpeed = vehicle.getDataTracker().get(com.gl.vehicles.entity.AbstractVehicleEntity.MAX_SPEED_SYNC);
            float speedPct = MathHelper.clamp(maxSpeed / 1.5f, 0, 1);
            context.drawText(this.textRenderer, Text.literal("VELOCIDAD"), statX, statY, 0x606060, false);
            statY += 10;
            context.fill(statX, statY, statX + barWidth, statY + barHeight, 0x44000000);
            context.fill(statX, statY, statX + (int) (barWidth * speedPct), statY + barHeight, 0xFF00AAFF);
            statY += (vehicle instanceof com.gl.vehicles.entity.KartEntity) ? 8 : 7;

            // --- 2. Accel ---
            float accel = vehicle.getDataTracker().get(com.gl.vehicles.entity.AbstractVehicleEntity.ACCEL_SYNC);
            float accelPct = MathHelper.clamp(accel / 0.05f, 0, 1);
            context.drawText(this.textRenderer, Text.literal("ACELERAC."), statX, statY, 0x606060, false);
            statY += 10;
            context.fill(statX, statY, statX + barWidth, statY + barHeight, 0x44000000);
            context.fill(statX, statY, statX + (int) (barWidth * accelPct), statY + barHeight, 0xFFFFAA00);
            statY += (vehicle instanceof com.gl.vehicles.entity.KartEntity) ? 8 : 7;

            // --- 3. Grip (Agarre) ---
            float gripPct = MathHelper.clamp(vehicle.getGrip() / 1.5f, 0, 1);
            context.drawText(this.textRenderer, Text.literal("AGARRE"), statX, statY, 0x606060, false);
            statY += 10;
            context.fill(statX, statY, statX + barWidth, statY + barHeight, 0x44000000);
            context.fill(statX, statY, statX + (int) (barWidth * gripPct), statY + barHeight, 0xFF00FF44);

            // --- Barra de Chasis ---
            float health = vehicle.getChassisHealth();
            float pct = health / com.gl.vehicles.entity.AbstractVehicleEntity.MAX_CHASSIS_HEALTH;
            boolean destroyed = vehicle.isDestroyed();

            int bx = 110;
            int by = 8;
            int hpBarW = 56;
            int hpBarH = 6;

            context.drawText(this.textRenderer, net.minecraft.text.Text.literal("CHASIS HP"), bx, by, 0x606060, false);
            by += 9;
            context.fill(bx - 1, by - 1, bx + hpBarW + 1, by + hpBarH + 1, 0xFF2B2B2B);
            int hpBarColor = destroyed ? 0xFF333333
                    : (pct > 0.5f ? 0xFF44CC44 : (pct > 0.25f ? 0xFFFFCC00 : 0xFFEE2222));
            context.fill(bx, by, bx + (int) (hpBarW * pct), by + hpBarH, hpBarColor);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
