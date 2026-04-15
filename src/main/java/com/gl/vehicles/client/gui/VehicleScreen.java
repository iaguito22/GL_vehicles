package com.gl.vehicles.client.gui;

import com.gl.vehicles.gui.VehicleScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VehicleScreen extends HandledScreen<VehicleScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    public VehicleScreen(VehicleScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 239; 
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        
        // Header + 4 slots de tractor (17 de header + 18*4 de slots = 89px)
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, 89);
        
        // Parche para ocultar los slots extra de la derecha en la zona de componentes
        context.fill(x + 26, y + 17, x + 169, y + 89, 0xFFC6C6C6);
        
        // 3 Filas exactas para el remolque (18*3 = 54px). Empezamos en v=17 para que sean slots limpios.
        context.drawTexture(TEXTURE, x, y + 89, 0, 17, this.backgroundWidth, 54);
        
        // Inventario del jugador (Empezamos en v=126 que tiene el titulo "Inventory" y los 36 slots)
        context.drawTexture(TEXTURE, x, y + 143, 0, 126, this.backgroundWidth, 96);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, 8, 6, 4210752, false);
        
        com.gl.vehicles.entity.AbstractVehicleEntity vehicle = this.handler.getVehicle();
        if (vehicle != null) {
            int textColor = 0x404040;
            context.drawText(this.textRenderer, net.minecraft.text.Text.literal("Acc. Frontal"), 28, 22, textColor, false);
            context.drawText(this.textRenderer, net.minecraft.text.Text.literal("Motor"),        28, 40, textColor, false);
            context.drawText(this.textRenderer, net.minecraft.text.Text.literal("Ruedas"),       28, 58, textColor, false);
            context.drawText(this.textRenderer, net.minecraft.text.Text.literal("Acc. Trasero"), 28, 76, textColor, false);

            // --- Barra de Chasis ---
            float health = vehicle.getChassisHealth();
            float pct = health / com.gl.vehicles.entity.AbstractVehicleEntity.MAX_CHASSIS_HEALTH;
            boolean destroyed = vehicle.isDestroyed();

            int bx = 122; // Posición X dentro del foreground (coordenadas relativas)
            int by = 8;
            int barW = 44;
            int barH = 6;

            // Label
            context.drawText(this.textRenderer, net.minecraft.text.Text.literal("Chasis HP"), bx, by, 0x606060, false);
            by += 9;

            // Fondo
            context.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, 0xFF2B2B2B);
            // Barra (roja si destruido, verde -> amarillo -> rojo según nivel)
            int barColor = destroyed ? 0xFF333333 :
                pct > 0.5f ? 0xFF44CC44 :
                pct > 0.25f ? 0xFFFFCC00 : 0xFFEE2222;
            context.fill(bx, by, bx + (int)(barW * pct), by + barH, barColor);

            if (destroyed) {
                context.drawText(this.textRenderer,
                    net.minecraft.text.Text.literal("DESTRUIDO").formatted(net.minecraft.util.Formatting.RED),
                    bx - 10, by + barH + 3, 0xFFFF3333, false);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
