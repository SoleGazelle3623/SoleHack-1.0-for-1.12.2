package com.yourwebsitespace.solehack.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class RenderUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void startSmooth() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }

    public static void endSmooth() {
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }

    public static void drawEntityBox(Entity entity, Color color, boolean filled) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        float alpha = filled ? 0.25f : 1.0f;
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB bb = entity.getEntityBoundingBox().expand(0.1, 0.1, 0.1).offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y, z);

        RenderGlobal.drawSelectionBoundingBox(bb, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1.0f);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}