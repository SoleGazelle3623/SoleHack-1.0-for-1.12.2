package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class WallHack extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Configuration parameters
    public double trackingRange = 64.0; // Distance to search for player boxes

    public WallHack() {
        super("WallHack", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || !this.isEnabled()) {
            return;
        }

        // Initialize GL state matrix settings
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        // CRUCIAL WALLHACK SETTING: Disabling the depth buffer forces geometry to render on top of all solid blocks
        GlStateManager.disableDepth();

        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f); // Width threshold of the box outline frames

        // Iterate through all loaded players inside the active chunk grid
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == mc.player || player.isDead || player.getHealth() <= 0) {
                continue; // Skip ourselves and dead targets
            }

            if (mc.player.getDistance(player) > trackingRange) {
                continue; // Skip players outside our configuration boundary limits
            }

            // Interpolate coordinates cleanly to prevent stuttering boxes when players move at high speeds
            double renderX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks() - mc.getRenderManager().viewerPosX;
            double renderY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks() - mc.getRenderManager().viewerPosY;
            double renderZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks() - mc.getRenderManager().viewerPosZ;

            // Generate an exact matching 3D bounding box layout around the player model size specifications
            double halfWidth = player.width / 2.0;
            AxisAlignedBB bb = new AxisAlignedBB(
                    renderX - halfWidth, renderY, renderZ - halfWidth,
                    renderX + halfWidth, renderY + player.height, renderZ + halfWidth
            );

            // Default Color: Soft Cyan/Blue for regular enemy players
            float r = 0.0f;
            float g = 0.7f;
            float b = 1.0f;

            // Optional structural hook layout for low-health targets (Turns red if below 5 hearts)
            if (player.getHealth() + player.getAbsorptionAmount() <= 10.0f) {
                r = 1.0f; g = 0.2f; b = 0.2f;
            }

            // Render a clean translucent selection box fill and solid outline visible through any obstruction
            RenderGlobal.renderFilledBox(bb, r, g, b, 0.12f);
            RenderGlobal.drawSelectionBoundingBox(bb, r, g, b, 0.8f);
        }

        // Return state machine indicators cleanly back to vanilla engine standards
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth(); // Re-enable depth testing so standard blocks render properly again
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected void onUpdate() {}
}
