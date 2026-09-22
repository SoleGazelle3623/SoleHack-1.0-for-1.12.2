package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StorageESP extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public StorageESP() {
        super("StorageESP", Category.RENDER);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Save current OpenGL state and disable depth/lighting for ESP effect
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.5F);

        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        for (TileEntity tileEntity : mc.world.loadedTileEntityList) {
            BlockPos pos = tileEntity.getPos();
            double x = pos.getX() - renderPosX;
            double y = pos.getY() - renderPosY;
            double z = pos.getZ() - renderPosZ;

            AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);

            // Color coding based on container type
            if (tileEntity instanceof TileEntityChest) {
                // Regular Chest: Orange / Gold
                drawStorageBox(bb, 1.0F, 0.6F, 0.0F, 0.4F, true);
            } else if (tileEntity instanceof TileEntityEnderChest) {
                // Ender Chest: Purple / Magenta
                drawStorageBox(bb, 0.8F, 0.2F, 1.0F, 0.4F, true);
            } else if (tileEntity instanceof TileEntityShulkerBox) {
                // Shulker Box: Cyan / Sky Blue
                drawStorageBox(bb, 0.0F, 0.8F, 1.0F, 0.4F, true);
            } else if (tileEntity instanceof TileEntityDispenser || tileEntity instanceof TileEntityDropper || tileEntity instanceof TileEntityHopper) {
                // Automation: Gray
                drawStorageBox(bb, 0.6F, 0.6F, 0.6F, 0.3F, false);
            } else if (tileEntity instanceof TileEntityFurnace) {
                // Furnace: Red / Orange
                drawStorageBox(bb, 1.0F, 0.2F, 0.0F, 0.3F, false);
            }
        }

        // Restore OpenGL state
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawStorageBox(AxisAlignedBB bb, float red, float green, float blue, float alpha, boolean fill) {
        // Draw outline
        RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, alpha * 2.0F);

        // Optional translucent box fill
        if (fill) {
            RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, alpha * 0.5F);
        }
    }

    @Override
    protected void onUpdate() {

    }
}
