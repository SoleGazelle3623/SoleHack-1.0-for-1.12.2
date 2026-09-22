package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CrystalAura extends Module {

    public static CrystalAura INSTANCE;
    private final Minecraft mc = Minecraft.getMinecraft();

    public double range = 5.0;
    public double wallsRange = 3.5;
    public boolean strictRotations = true;
    public boolean protocol13 = true;
    public boolean silentSwitch = true;

    // Delay configurations (in milliseconds)
    public int placeDelay = 0;
    public int breakDelay = 0;

    private long lastPlaceTime = 0;
    private long lastBreakTime = 0;

    private BlockPos renderPos = null;

    public CrystalAura() {
        super("CrystalAura", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        renderPos = null;
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        renderPos = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            renderPos = null;
            return;
        }

        EntityPlayer target = getClosestTarget();
        if (target == null) {
            renderPos = null;
            return;
        }

        // Single-tick double burst execution powered by Mixin prediction
        breakCrystals(target);
        placeCrystal(target);
    }

    private EntityPlayer getClosestTarget() {
        return mc.world.playerEntities.stream()
                .filter(player -> player != mc.player && !player.isDead && player.getHealth() > 0)
                .filter(player -> mc.player.getDistance(player) <= range)
                .min(Comparator.comparingDouble(player -> mc.player.getDistance(player)))
                .orElse(null);
    }

    private boolean breakCrystals(EntityPlayer target) {
        if (System.currentTimeMillis() - lastBreakTime < breakDelay) {
            return false;
        }

        List<EntityEnderCrystal> crystals = mc.world.loadedEntityList.stream()
                .filter(EntityEnderCrystal.class::isInstance)
                .map(EntityEnderCrystal.class::cast)
                .filter(crystalEntity -> {
                    double reach = mc.player.canEntityBeSeen(crystalEntity) ? range : wallsRange;
                    return mc.player.getDistance(crystalEntity) <= reach;
                })
                .sorted(Comparator.comparingDouble(crystalEntity -> target.getDistance(crystalEntity)))
                .collect(Collectors.toList());

        if (crystals.isEmpty()) return false;

        EntityEnderCrystal crystal = crystals.get(0);

        if (strictRotations) {
            lookAtVector(crystal.getPositionVector());
        }

        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        mc.player.swingArm(EnumHand.MAIN_HAND);

        lastBreakTime = System.currentTimeMillis();
        return true;
    }

    private void placeCrystal(EntityPlayer target) {
        if (System.currentTimeMillis() - lastPlaceTime < placeDelay) {
            return;
        }

        int crystalSlot = findCrystalSlot();
        if (crystalSlot == -1) {
            renderPos = null;
            return;
        }

        BlockPos bestPos = null;
        double bestDist = range * range;

        int targetX = MathHelper.floor(target.posX);
        int targetY = MathHelper.floor(target.posY);
        int targetZ = MathHelper.floor(target.posZ);

        for (int x = targetX - 3; x <= targetX + 3; x++) {
            for (int y = targetY - 2; y <= targetY + 2; y++) {
                for (int z = targetZ - 3; z <= targetZ + 3; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (isValidBlock(pos)) {
                        double dist = mc.player.getDistanceSqToCenter(pos);

                        boolean canSee = mc.world.rayTraceBlocks(
                                new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                                new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), false, true, false) == null;

                        if (!canSee && dist > (wallsRange * wallsRange)) continue;

                        if (dist <= bestDist) {
                            bestDist = dist;
                            bestPos = pos;
                        }
                    }
                }
            }
        }

        if (bestPos != null) {
            renderPos = bestPos;
            int oldSlot = mc.player.inventory.currentItem;

            if (strictRotations) {
                lookAtVector(new Vec3d(bestPos.getX() + 0.5, bestPos.getY() + 1, bestPos.getZ() + 0.5));
            }

            if (silentSwitch && crystalSlot != oldSlot) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(crystalSlot));
            } else {
                mc.player.inventory.currentItem = crystalSlot;
            }

            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    bestPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f));
            mc.player.swingArm(EnumHand.MAIN_HAND);

            if (silentSwitch && crystalSlot != oldSlot) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            }

            lastPlaceTime = System.currentTimeMillis();
        } else {
            renderPos = null;
        }
    }

    private boolean isValidBlock(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN &&
                mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
            return false;
        }

        BlockPos up1 = pos.up();
        BlockPos up2 = pos.up(2);

        if (protocol13) {
            if (!mc.world.isAirBlock(up1)) return false;
        } else {
            if (!mc.world.isAirBlock(up1) || !mc.world.isAirBlock(up2)) return false;
        }

        double maxY = protocol13 ? 1.0 : 2.0;
        AxisAlignedBB box = new AxisAlignedBB(
                up1.getX(), up1.getY(), up1.getZ(),
                up1.getX() + 1.0, up1.getY() + maxY, up1.getZ() + 1.0
        );

        return mc.world.getEntitiesWithinAABB(Entity.class, box).isEmpty();
    }

    private void lookAtVector(Vec3d targetVec) {
        double diffX = targetVec.x - mc.player.posX;
        double diffY = targetVec.y - (mc.player.posY + mc.player.getEyeHeight());
        double diffZ = targetVec.z - mc.player.posZ;
        double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(diffY, diffXZ) * 180.0 / Math.PI);

        float serverYaw = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        float serverPitch = mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
        serverPitch = MathHelper.clamp(serverPitch, -90.0f, 90.0f);

        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(serverYaw, serverPitch, mc.player.onGround));
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                return i;
            }
        }
        return -1;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || renderPos == null || !this.isEnabled()) {
            return;
        }

        double renderX = renderPos.getX() - mc.getRenderManager().viewerPosX;
        double renderY = renderPos.getY() - mc.getRenderManager().viewerPosY;
        double renderZ = renderPos.getZ() - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB bb = new AxisAlignedBB(renderX, renderY, renderZ, renderX + 1.0, renderY + 1.0, renderZ + 1.0);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);

        RenderGlobal.renderFilledBox(bb, 1.0f, 0.0f, 0.5f, 0.15f);
        RenderGlobal.drawSelectionBoundingBox(bb, 1.0f, 0.0f, 0.5f, 0.8f);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix(); // FIXED
    }

    @Override
    protected void onUpdate() {}
}