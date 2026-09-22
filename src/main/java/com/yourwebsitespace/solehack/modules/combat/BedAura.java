package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
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

public class BedAura extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Automated Combat Configuration Profiles
    public double range = 5.0;
    public boolean strictRotations = true;
    public long explodedelayMs = 80;

    private long lastExplodeTime = 0L;

    // Coordinates cached for the active render tracking pass
    private BlockPos renderPos = null;
    private EnumFacing renderFacing = EnumFacing.NORTH;

    public BedAura() {
        super("BedAura", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        lastExplodeTime = 0L;
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

        if (mc.player.dimension == 0) {
            renderPos = null;
            return;
        }

        EntityPlayer target = getClosestTarget();
        if (target == null) {
            renderPos = null;
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastExplodeTime < explodedelayMs) {
            return;
        }

        if (explodeExistingBeds()) {
            lastExplodeTime = currentTime;
            return;
        }

        if (placeBedNearTarget(target)) {
            lastExplodeTime = currentTime;
        }
    }

    private EntityPlayer getClosestTarget() {
        return mc.world.playerEntities.stream()
                .filter(player -> player != mc.player && !player.isDead && player.getHealth() > 0)
                .filter(player -> mc.player.getDistance(player) <= range)
                .min(Comparator.comparingDouble(player -> mc.player.getDistance(player)))
                .orElse(null);
    }

    private boolean explodeExistingBeds() {
        for (TileEntity tile : mc.world.loadedTileEntityList) {
            if (tile instanceof TileEntityBed) {
                BlockPos bedPos = tile.getPos();
                if (mc.player.getDistanceSqToCenter(bedPos) <= range * range) {

                    if (strictRotations) {
                        lookAtVector(new Vec3d(bedPos.getX() + 0.5, bedPos.getY() + 0.5, bedPos.getZ() + 0.5));
                    }

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                            bedPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeBedNearTarget(EntityPlayer target) {
        int bedSlot = findBedSlot();
        if (bedSlot == -1) return false;

        // FIXED: Explicitly anchors tracking calculations to the target's grounding plane vector coordinate [5b5t.org]
        BlockPos targetFeetPos = new BlockPos(Math.floor(target.posX), Math.floor(target.posY), Math.floor(target.posZ));
        BlockPos placePos = null;
        EnumFacing placeFacing = EnumFacing.NORTH;
        double bestPlacementDistance = Double.MAX_VALUE;

        // GROUND HORIZONTAL MATRIX SCANNER: Sweeps the surrounding floor layout up to 3 blocks away [5b5t.org]
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                // Adjust for uneven block elevations (-1 block depth to accommodate half slabs or steps) [5b5t.org]
                for (int y = -1; y <= 1; y++) {
                    BlockPos floorCandidate = targetFeetPos.add(x, y, z);

                    // Validate that the target space is open air, but rests strictly on a solid block surface [5b5t.org]
                    if (mc.world.isAirBlock(floorCandidate) && !mc.world.isAirBlock(floorCandidate.down())) {
                        double localPlayerDistSq = mc.player.getDistanceSqToCenter(floorCandidate);

                        // Verify the floor candidate block is within your overall reach constraints [5b5t.org]
                        if (localPlayerDistSq <= range * range) {

                            // Check all four cardinal placement facings to find the path closest to the target player model
                            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                                BlockPos footSegmentPos = floorCandidate.offset(facing);

                                // A bed takes 2 horizontal blocks. Verify the second structural block space is also open air! [5b5t.org]
                                if (mc.world.isAirBlock(footSegmentPos)) {
                                    double targetDistance = target.getDistanceSqToCenter(floorCandidate);

                                    if (targetDistance < bestPlacementDistance) {
                                        bestPlacementDistance = targetDistance;
                                        placePos = floorCandidate;
                                        placeFacing = facing;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (placePos != null) {
            int currentSlot = mc.player.inventory.currentItem;
            BlockPos supportBlock = placePos.down();

            if (strictRotations) {
                lookAtVector(new Vec3d(supportBlock.getX() + 0.5, supportBlock.getY() + 1.0, supportBlock.getZ() + 0.5));
            }

            // Cache vectors securely to process the 3D ESP outline pass
            renderPos = placePos;
            renderFacing = placeFacing;

            // Ghost hand hotbar packet swap pipeline
            mc.player.connection.sendPacket(new CPacketHeldItemChange(bedSlot));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    supportBlock, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f));
            mc.player.connection.sendPacket(new CPacketHeldItemChange(currentSlot));

            mc.player.swingArm(EnumHand.MAIN_HAND);
            return true;
        }

        return false;
    }

    private int findBedSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBed) {
                return i;
            }
        }
        return -1;
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

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.player == null || mc.world == null || renderPos == null || !this.isEnabled()) {
            return;
        }

        double renderX = renderPos.getX() - mc.getRenderManager().viewerPosX;
        double renderY = renderPos.getY() - mc.getRenderManager().viewerPosY;
        double renderZ = renderPos.getZ() - mc.getRenderManager().viewerPosZ;

        double footX = renderX + renderFacing.getDirectionVec().getX();
        double footZ = renderZ + renderFacing.getDirectionVec().getZ();

        double minX = Math.min(renderX, footX);
        double maxX = Math.max(renderX, footX) + 1.0;
        double minZ = Math.min(renderZ, footZ);
        double maxZ = Math.max(renderZ, footZ) + 1.0;

        AxisAlignedBB bedBoxBB = new AxisAlignedBB(minX, renderY, minZ, maxX, renderY + 0.5625, maxZ);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);
        RenderGlobal.renderFilledBox(bedBoxBB, 1.0f, 0.2f, 0.2f, 0.15f);
        RenderGlobal.drawSelectionBoundingBox(bedBoxBB, 1.0f, 0.2f, 0.2f, 0.8f);
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
