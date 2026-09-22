package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogoutSpots extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    public static LogoutSpots INSTANCE;

    public final List<LogoutSpot> spots = new ArrayList<>();
    private final Set<String> previousPlayers = new HashSet<>();

    public LogoutSpots() {
        super("LogoutSpots", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        spots.clear();
        previousPlayers.clear();
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        spots.clear();
        previousPlayers.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) return;
        if (!this.isEnabled()) return;

        Set<String> currentPlayers = new HashSet<>();
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == mc.player) continue;
            currentPlayers.add(player.getName());

            // Remove from logout spots if they reconnect or come back into range
            spots.removeIf(spot -> spot.name.equalsIgnoreCase(player.getName()));
        }

        // Cleaned up empty statement check
        for (String playerName : previousPlayers) {
            if (!currentPlayers.contains(playerName)) {
                // Handle player disappearance if desired
            }
        }

        previousPlayers.clear();
        previousPlayers.addAll(currentPlayers);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || spots.isEmpty()) return;

        for (LogoutSpot spot : spots) {
            renderLogoutBox(spot);
            renderNameTag(spot);
        }
    }

    private void renderLogoutBox(LogoutSpot spot) {
        AxisAlignedBB bb = new AxisAlignedBB(
                spot.position.x - 0.3, spot.position.y, spot.position.z - 0.3,
                spot.position.x + 0.3, spot.position.y + 1.8, spot.position.z + 0.3
        );

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        AxisAlignedBB translatedBB = bb.offset(-renderPosX, -renderPosY, -renderPosZ);

        // Standard 1.12.2 BufferBuilder box renderer
        drawBoundingBox(translatedBB, 1.0f, 0.3f, 0.3f, 0.3f);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawBoundingBox(AxisAlignedBB bb, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        // Draw outline edges
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();

        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

        buffer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

        buffer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        buffer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        tessellator.draw();
    }

    private void renderNameTag(LogoutSpot spot) {
        double x = spot.position.x - mc.getRenderManager().viewerPosX;
        double y = spot.position.y + 2.0 - mc.getRenderManager().viewerPosY;
        double z = spot.position.z - mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.025f;
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        String text = spot.name + " (Logout)";
        int width = mc.fontRenderer.getStringWidth(text) / 2;

        mc.fontRenderer.drawStringWithShadow(text, -width, 0, 0xFFFF5555);

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static class LogoutSpot {
        public final String name;
        public final Vec3d position;
        public final long timestamp;

        public LogoutSpot(String name, Vec3d position) {
            this.name = name;
            this.position = position;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @Override
    protected void onUpdate() {}
}