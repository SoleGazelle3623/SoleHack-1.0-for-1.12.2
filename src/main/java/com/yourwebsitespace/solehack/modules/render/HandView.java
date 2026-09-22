package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandView extends Module {

    // scale of 0.4 = viewmodel rendered 2.5x smaller (1 / 0.4 = 2.5)
    private float scale = 0.4f;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float offsetZ = 0f;

    // Compensates for GlStateManager.scale() also pulling the model
    // closer to the camera. Without this, shrinking the model also
    // shrinks its distance from the eye, which both hides the size
    // change and exaggerates bob/swing motion (the hand sweeps a
    // bigger portion of the view because it's now much closer).
    // Tune this value live in-game via HandViewConfigScreen until
    // the model shrinks in place without drifting toward/away from
    // the camera - the "right" number depends on your FOV and vanilla's
    // hand render distance, so it isn't a fixed constant.
    private float depthCompensation = 8.0f;

    public HandView() {
        super("HandView", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        // Don't cancel - this runs inside vanilla's own push/pop matrix
        // scope, so the transform below only affects this render pass
        // and vanilla cleans it up automatically right after.
        GlStateManager.translate(offsetX, offsetY, offsetZ);
        GlStateManager.scale(scale, scale, scale);

        // Push the model back out along Z to restore its original
        // distance from the camera after the scale pulled it closer.
        if (scale > 0f) {
            float compensation = depthCompensation * (1f - scale) / scale;
            GlStateManager.translate(0f, 0f, compensation);
        }
    }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

    public float getOffsetX() { return offsetX; }
    public void setOffsetX(float offsetX) { this.offsetX = offsetX; }

    public float getOffsetY() { return offsetY; }
    public void setOffsetY(float offsetY) { this.offsetY = offsetY; }

    public float getOffsetZ() { return offsetZ; }
    public void setOffsetZ(float offsetZ) { this.offsetZ = offsetZ; }

    public float getDepthCompensation() { return depthCompensation; }
    public void setDepthCompensation(float depthCompensation) { this.depthCompensation = depthCompensation; }
}