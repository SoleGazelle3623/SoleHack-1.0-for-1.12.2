package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityEnderCrystal;

import java.awt.Color;

public class CrystalColor extends Module {

    private boolean rainbow = false;
    private int solidColor = 0x55FFFF; // default cyan, 0xRRGGBB
    private float rainbowSpeedSeconds = 4.0f; // seconds per full hue cycle

    // Stashed so we can restore vanilla rendering on disable
    private Render<EntityEnderCrystal> originalRenderer;

    public CrystalColor() {
        super("CrystalColor", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        originalRenderer = renderManager.getEntityClassRenderObject(EntityEnderCrystal.class);
        // NOTE: entityRenderMap is a public field on RenderManager in MCP-mapped
        // 1.12.2 dev environments. If your mappings differ and this doesn't
        // resolve, swapping the renderer at runtime will need reflection instead.
        renderManager.entityRenderMap.put(EntityEnderCrystal.class, new TintedCrystalRenderer(renderManager, this));
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        if (originalRenderer != null) {
            Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(EntityEnderCrystal.class, originalRenderer);
            originalRenderer = null;
        }
    }

    public boolean isRainbow() { return rainbow; }
    public void setRainbow(boolean rainbow) { this.rainbow = rainbow; }

    public int getSolidColor() { return solidColor; }
    public void setSolidColor(int solidColor) { this.solidColor = solidColor; }

    public float getRainbowSpeedSeconds() { return rainbowSpeedSeconds; }
    public void setRainbowSpeedSeconds(float rainbowSpeedSeconds) { this.rainbowSpeedSeconds = rainbowSpeedSeconds; }

    private static class TintedCrystalRenderer extends RenderEnderCrystal {

        private final CrystalColor module;

        TintedCrystalRenderer(RenderManager renderManager, CrystalColor module) {
            super(renderManager);
            this.module = module;
        }

        @Override
        public void doRender(EntityEnderCrystal entity, double x, double y, double z, float entityYaw, float partialTicks) {
            float r, g, b;

            if (module.isRainbow()) {
                float cycleSeconds = Math.max(0.1f, module.getRainbowSpeedSeconds());
                float hue = (System.currentTimeMillis() % (long) (cycleSeconds * 1000)) / (cycleSeconds * 1000f);
                int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
                r = ((rgb >> 16) & 0xFF) / 255f;
                g = ((rgb >> 8) & 0xFF) / 255f;
                b = (rgb & 0xFF) / 255f;
            } else {
                int c = module.getSolidColor();
                r = ((c >> 16) & 0xFF) / 255f;
                g = ((c >> 8) & 0xFF) / 255f;
                b = (c & 0xFF) / 255f;
            }

            GlStateManager.color(r, g, b, 1.0f);
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f); // reset so nothing else inherits the tint
        }
    }
}