package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Tracers extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public Tracers() {
        super("Tracers", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        super.onEnable();
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        super.onDisable();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null) return;

        float partialTicks = event.getPartialTicks();

        // Retrieve internal camera positions from RenderManager
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        // Establish look vector for center-screen tracing alignment
        Vec3d cameraLook = new Vec3d(0, 0, 1)
                .rotatePitch(-(float)Math.toRadians(mc.player.rotationPitch))
                .rotateYaw(-(float)Math.toRadians(mc.player.rotationYaw));

        // OpenGL Setup
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.glLineWidth(1.5f);

        for (EntityPlayer targetPlayer : mc.world.playerEntities) {
            // Filter target criteria
            if (targetPlayer == mc.player || targetPlayer.isDead || targetPlayer.isInvisible()) {
                continue;
            }

            // Interpolate target positions over current partial ticks
            double targetX = targetPlayer.lastTickPosX + (targetPlayer.posX - targetPlayer.lastTickPosX) * partialTicks - renderPosX;
            double targetY = targetPlayer.lastTickPosY + (targetPlayer.posY - targetPlayer.lastTickPosY) * partialTicks - renderPosY;
            double targetZ = targetPlayer.lastTickPosZ + (targetPlayer.posZ - targetPlayer.lastTickPosZ) * partialTicks - renderPosZ;

            // Render Tracer Line
            GlStateManager.color(0.0f, 1.0f, 0.0f, 0.6f); // Green tracer lines

            GL11.glBegin(GL11.GL_LINES);
            // Screen vector origin point
            GL11.glVertex3d(cameraLook.x, cameraLook.y + mc.player.getEyeHeight(), cameraLook.z);
            // Player feet location destination point
            GL11.glVertex3d(targetX, targetY, targetZ);
            GL11.glEnd();
        }

        // Cleanup state machines
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
