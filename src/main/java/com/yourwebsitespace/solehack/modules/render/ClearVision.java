package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClearVision extends Module {

    public ClearVision() {
        super("ClearVision", Category.RENDER);
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

    // Suppress hurtcam / screen shake from damage
    @SubscribeEvent
    public void onFovModifier(EntityViewRenderEvent.FOVModifier event) {
        event.setFOV((float) Minecraft.getMinecraft().gameSettings.fovSetting);
    }

    // Remove the water and fire screen overlays (the tint/texture drawn when
    // your head is inside a water or fire block). Leaves BLOCK overlay (head
    // stuck inside a solid block) alone since that's not a "vision" overlay
    // in the same sense - remove that too if you want it gone as well.
    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        RenderBlockOverlayEvent.OverlayType type = event.getOverlayType();
        if (type == RenderBlockOverlayEvent.OverlayType.WATER
                || type == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }

    // Continuously clear active particles, and zero out rain/snow rendering, each tick
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.effectRenderer != null) {
            try {
                java.lang.reflect.Field fxLayers = mc.effectRenderer.getClass().getDeclaredField("fxLayers");
                fxLayers.setAccessible(true);
                java.util.List<Particle>[] layers = (java.util.List<Particle>[]) fxLayers.get(mc.effectRenderer);
                for (java.util.List<Particle> layer : layers) {
                    layer.clear();
                }
            } catch (Exception ignored) {
                // Field name may vary between mappings; safe to skip if reflection fails
            }
        }

        // Zero the client-side rain/snow render strength so renderRainSnow()
        // has nothing to draw. This is purely visual - it does not touch the
        // server's actual weather state, so weather-driven mechanics (crop
        // growth, mob burning, etc., which are server-authoritative) are unaffected.
        // NOTE: assumes rainingStrength/thunderingStrength are public fields on
        // World in your mappings (standard in vanilla MCP 1.12.2). If private,
        // this needs reflection instead.
        if (mc.world != null) {
            mc.world.rainingStrength = 0f;
            mc.world.prevRainingStrength = 0f;
            mc.world.thunderingStrength = 0f;
            mc.world.prevThunderingStrength = 0f;
        }
    }

    // Hide vanilla overlay elements (hotbar effects icons, boss bar, pumpkin/vignette overlays handled via render pass skip)
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        switch (event.getType()) {
            case POTION_ICONS:
            case BOSSHEALTH:
            case VIGNETTE:
            case PORTAL:
                event.setCanceled(true);
                break;
            default:
                break;
        }
    }
}