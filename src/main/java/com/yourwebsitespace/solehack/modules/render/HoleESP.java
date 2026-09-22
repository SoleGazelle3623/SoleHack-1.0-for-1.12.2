package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
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

public class HoleESP extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Configurable rendering properties
    public int scanRange = 8; // Sphere radius block field size to scan around the player

    private final List<HoleInfo> safeHoles = new ArrayList<>();

    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        synchronized (safeHoles) {
            safeHoles.clear();
        }
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        synchronized (safeHoles) {
            safeHoles.clear();
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

        List<HoleInfo> tempHoles = new ArrayList<>();

        int playerX = MathHelper.floor(mc.player.posX);
        int playerY = MathHelper.floor(mc.player.posY);
        int playerZ = MathHelper.floor(mc.player.posZ);

        // Raw 3D integer coordinate traversal iteration loop (Prevents BlockPos collection bugs)
        for (int x = playerX - scanRange; x <= playerX + scanRange; x++) {
            for (int y = playerY - 4; y <= playerY + 4; y++) { // Limit Y-axis to save performance frames
                for (int z = playerZ - scanRange; z <= playerZ + scanRange; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // A valid hole must be an open air block, with open air directly above it too
                    if (mc.world.isAirBlock(pos) && mc.world.isAirBlock(pos.up()) && mc.world.isAirBlock(pos.up(2))) {
                        HoleType type = checkHoleType(pos);
                        if (type != HoleType.NONE) {
                            tempHoles.add(new HoleInfo(pos, type));
                        }
                    }
                }
            }
        }

        synchronized (safeHoles) {
            safeHoles.clear();
            safeHoles.addAll(tempHoles);
        }
    }

    private HoleType checkHoleType(BlockPos pos) {
        BlockPos[] sides = new BlockPos[] {
                pos.down(), pos.north(), pos.south(), pos.east(), pos.west()
        };

        boolean isBedrock = true;
        boolean isObsidian = true;

        for (BlockPos side : sides) {
            net.minecraft.block.Block block = mc.world.getBlockState(side).getBlock();

            if (block != Blocks.BEDROCK) {
                isBedrock = false; // If even 1 wall is obsidian, it's not a pure Bedrock hole
            }
            if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
                isObsidian = false; // If any wall is dirt/stone/air, it's unsafe from crystals
            }
        }

        if (isBedrock) return HoleType.BEDROCK;
        if (isObsidian) return HoleType.OBSIDIAN;

        return HoleType.NONE;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || !this.isEnabled()) {
            return;
        }

        List<HoleInfo> renderTargets;
        synchronized (safeHoles) {
            if (safeHoles.isEmpty()) return;
            renderTargets = new ArrayList<>(safeHoles);
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);

        for (HoleInfo hole : renderTargets) {
            BlockPos pos = hole.pos;

            // Calculate absolute relative positioning offset against camera viewer metrics
            double renderX = pos.getX() - mc.getRenderManager().viewerPosX;
            double renderY = pos.getY() - mc.getRenderManager().viewerPosY;
            double renderZ = pos.getZ() - mc.getRenderManager().viewerPosZ;

            // Box shape spans only 0.2 blocks high along the ground floor layer of the hole
            AxisAlignedBB bb = new AxisAlignedBB(renderX, renderY, renderZ, renderX + 1.0, renderY + 0.2, renderZ + 1.0);

            // Red/Green color assignment profile properties
            float r = (hole.type == HoleType.BEDROCK) ? 0.0f : 1.0f; // Green if Bedrock, Red if Obsidian
            float g = (hole.type == HoleType.BEDROCK) ? 1.0f : 0.0f;
            float b = 0.0f;

            // Render clear translucent box floor and outer skeleton frame wire meshes
            RenderGlobal.renderFilledBox(bb, r, g, b, 0.15f);
            RenderGlobal.drawSelectionBoundingBox(bb, r, g, b, 0.7f);
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected void onUpdate() {}

    // Inner framework data mapping enums and objects
    private enum HoleType { NONE, OBSIDIAN, BEDROCK }

    private static class HoleInfo {
        public final BlockPos pos;
        public final HoleType type;

        public HoleInfo(BlockPos pos, HoleType type) {
            this.pos = pos;
            this.type = type;
        }
    }
}
