package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;

public class KillAura extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t optimized targeting thresholds
    public double range = 4.2;        // Reach parameter threshold limit
    public double wallsRange = 3.0;   // Reach boundary if target is behind walls
    public long hitDelayMs = 625;      // High-speed CPS trigger throttle rate (approx. 11-12 CPS)
    public boolean strictRotations = true;

    private long lastHitTime = 0L;
    private EntityPlayer currentTarget = null;

    public KillAura() {
        super("KillAura", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        currentTarget = null;
        lastHitTime = 0L;
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        currentTarget = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            currentTarget = null;
            return;
        }

        // Find the best valid target player closest to us
        currentTarget = getClosestTarget();
        if (currentTarget == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // High-Precision hardware timer execution throttle
        if (currentTime - lastHitTime >= hitDelayMs) {

            // Send silent rotation look vector update packets right before swinging
            if (strictRotations) {
                lookAtEntityCenter(currentTarget);
            }

            // Dispatch raw attack packet sequence to the server stream
            mc.player.connection.sendPacket(new CPacketUseEntity(currentTarget));
            mc.player.swingArm(EnumHand.MAIN_HAND);

            lastHitTime = currentTime;
        }
    }

    private EntityPlayer getClosestTarget() {
        return mc.world.playerEntities.stream()
                .filter(player -> player != mc.player && !player.isDead && player.getHealth() > 0)
                .filter(player -> {
                    // Filter targets cleanly based on whether we can physically see them or not
                    double reach = mc.player.canEntityBeSeen(player) ? range : wallsRange;
                    return mc.player.getDistance(player) <= reach;
                })
                .min(Comparator.comparingDouble(player -> mc.player.getDistance(player)))
                .orElse(null);
    }

    private void lookAtEntityCenter(EntityPlayer target) {
        // Targets the physical center point location of the target player's hitbox bounding grid
        Vec3d targetVec = new Vec3d(
                target.posX,
                target.posY + (target.getEyeHeight() / 2.0),
                target.posZ
        );

        double diffX = targetVec.x - mc.player.posX;
        double diffY = targetVec.y - (mc.player.posY + mc.player.getEyeHeight());
        double diffZ = targetVec.z - mc.player.posZ;
        double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(diffY, diffXZ) * 180.0 / Math.PI);

        float serverYaw = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        float serverPitch = mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
        serverPitch = MathHelper.clamp(serverPitch, -90.0f, 90.0f);

        // Dispatches the direct rotation data fields straight down the network pipe
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(serverYaw, serverPitch, mc.player.onGround));
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || currentTarget == null || !this.isEnabled()) {
            return;
        }

        // Generate full 3D Box ESP highlights around our active combat target tracking bounding box coordinates
        double renderX = currentTarget.lastTickPosX + (currentTarget.posX - currentTarget.lastTickPosX) * event.getPartialTicks() - mc.getRenderManager().viewerPosX;
        double renderY = currentTarget.lastTickPosY + (currentTarget.posY - currentTarget.lastTickPosY) * event.getPartialTicks() - mc.getRenderManager().viewerPosY;
        double renderZ = currentTarget.lastTickPosZ + (currentTarget.posZ - currentTarget.lastTickPosZ) * event.getPartialTicks() - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB bb = new AxisAlignedBB(
                renderX - currentTarget.width / 2.0, renderY, renderZ - currentTarget.width / 2.0,
                renderX + currentTarget.width / 2.0, renderY + currentTarget.height, renderZ + currentTarget.width / 2.0
        );

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);

        // Render clear semi-transparent red status block box highlights
        RenderGlobal.renderFilledBox(bb, 1.0f, 0.0f, 0.0f, 0.15f);
        RenderGlobal.drawSelectionBoundingBox(bb, 1.0f, 0.0f, 0.0f, 0.8f);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected void onUpdate() {}
}
