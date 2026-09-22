package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import com.yourwebsitespace.solehack.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHud extends Module {

    private static int kills = 0;
    private static int deaths = 0;

    private static final float SCALE = 1.2f;

    public ClientHud() {
        super("ClientHud", Category.RENDER);
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
    public void onLivingDeath(LivingDeathEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (event.getEntityLiving() == mc.player) {
            deaths++;
        } else if (event.getEntityLiving().getAttackingEntity() == mc.player) {
            kills++;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        FontRenderer fr = mc.fontRenderer;

        GlStateManager.pushMatrix();
        GlStateManager.scale(SCALE, SCALE, 1f);

        // Scaled coordinate space: real screen pos / SCALE
        int x = (int) (5 / SCALE);
        int y = (int) (5 / SCALE);

        fr.drawStringWithShadow("SoleHack 1.0", x, y, 0x55FFFF);
        y += (fr.FONT_HEIGHT + 3);
        fr.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(), x, y, 0xFFFFFF);
        y += (fr.FONT_HEIGHT + 3);
        fr.drawStringWithShadow("Ping: " + getPing() + "ms", x, y, 0xFFFFFF);
        y += (fr.FONT_HEIGHT + 3);
        fr.drawStringWithShadow("K/D: " + kills + "/" + deaths, x, y, 0xFFFFFF);

        // Coordinates, pinned to the bottom-right corner of the screen.
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = (int) (sr.getScaledWidth() / SCALE);
        int screenHeight = (int) (sr.getScaledHeight() / SCALE);
        int margin = (int) (5 / SCALE);

        String coords = String.format("XYZ: %.1f, %.1f, %.1f",
                mc.player.posX, mc.player.posY, mc.player.posZ);
        int coordsX = screenWidth - fr.getStringWidth(coords) - margin;
        int coordsY = screenHeight - fr.FONT_HEIGHT - margin;
        fr.drawStringWithShadow(coords, coordsX, coordsY, 0xFFFFFF);

        // Enabled module list ("ArrayList"), right-aligned in the top-right
        // corner, stacked downward - the layout most clients use.
        List<Module> enabled = ModuleManager.getAll().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparing(m -> -fr.getStringWidth(m.getName())))
                .collect(Collectors.toList());

        int listY = (int) (5 / SCALE);
        for (Module module : enabled) {
            String name = module.getName();
            int listX = screenWidth - fr.getStringWidth(name) - margin;
            fr.drawStringWithShadow(name, listX, listY, 0xFFFFFF);
            listY += (fr.FONT_HEIGHT + 3);
        }

        GlStateManager.popMatrix();
    }

    private int getPing() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.getConnection() == null) return 0;

        NetworkPlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getGameProfile().getId());
        return info != null ? info.getResponseTime() : 0;
    }
}