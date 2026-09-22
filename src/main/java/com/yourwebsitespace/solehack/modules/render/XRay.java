package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class XRay extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Configuration parameters
    public int scanRange = 12; // Radius block reach to scan underground

    private final List<BlockPos> targetBlocks = new ArrayList<>();

    public XRay() {
        super("XRay", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        synchronized (targetBlocks) {
            targetBlocks.clear();
        }
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        synchronized (targetBlocks) {
            targetBlocks.clear();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        List<BlockPos> discoveredBlocks = new ArrayList<>();

        int playerX = MathHelper.floor(mc.player.posX);
        int playerY = MathHelper.floor(mc.player.posY);
        int playerZ = MathHelper.floor(mc.player.posZ);

        // Scan the 3D grid chunk field surrounding the player using safe primitives
        for (int x = playerX - scanRange; x <= playerX + scanRange; x++) {
            for (int y = Math.max(0, playerY - 6); y <= Math.min(255, playerY + 6); y++) { // Throttled Y-height to optimize client FPS
                for (int z = playerZ - scanRange; z <= playerZ + scanRange; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();

                    // Filter criteria list: Tracks rare ores and utility blocks hidden inside the terrain
                    if (block == Blocks.DIAMOND_ORE || block == Blocks.EMERALD_ORE || block == Blocks.CHEST || block == Blocks.ENDER_CHEST) {
                        discoveredBlocks.add(pos);
                    }
                }
            }
        }

        // Thread-safe generic allocation swap into cache loop array
        synchronized (targetBlocks) {
            targetBlocks.clear();
            targetBlocks.addAll(discoveredBlocks);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || !this.isEnabled()) {
            return;
        }

        List<BlockPos> renderList;
        synchronized (targetBlocks) {
            if (targetBlocks.isEmpty()) return;
            renderList = new ArrayList<>(targetBlocks);
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth(); // CRUCIAL: Disabling the depth buffer test forces graphics to display through solid blocks
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.5f);

        for (BlockPos pos : renderList) {
            // Translate absolute world variables into camera matrix relative rendering offsets
            double renderX = pos.getX() - mc.getRenderManager().viewerPosX;
            double renderY = pos.getY() - mc.getRenderManager().viewerPosY;
            double renderZ = pos.getZ() - mc.getRenderManager().viewerPosZ;

            AxisAlignedBB bb = new AxisAlignedBB(renderX, renderY, renderZ, renderX + 1.0, renderY + 1.0, renderZ + 1.0);

            // Fetch block definitions to paint targeted elements accurately
            Block type = mc.world.getBlockState(pos).getBlock();
            float r = 1.0f, g = 1.0f, b = 1.0f; // Default White marker

            if (type == Blocks.DIAMOND_ORE) { r = 0.0f; g = 0.8f; b = 1.0f; }      // Diamond Blue
            else if (type == Blocks.EMERALD_ORE) { r = 0.0f; g = 1.0f; b = 0.0f; } // Emerald Green
            else if (type == Blocks.CHEST || type == Blocks.ENDER_CHEST) { r = 1.0f; g = 0.5f; b = 0.0f; } // Chest Orange

            // Render crisp transparent fill and outer frame wireframes visible everywhere
            RenderGlobal.renderFilledBox(bb, r, g, b, 0.12f);
            RenderGlobal.drawSelectionBoundingBox(bb, r, g, b, 0.75f);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth(); // Return deep coordinate pipeline metrics back to vanilla defaults
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected void onUpdate() {}
}
