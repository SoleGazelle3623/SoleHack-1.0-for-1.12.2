package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AntiBedTrap extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Tactical Defensive Configurations
    public double range = 3.5;          // Range limits optimized for internal player hitbox bounding layers [5b5t.org]
    public boolean autoWeaponSwap = true; // Silent packet tool swap to use high efficiency pickaxes [5b5t.org]
    public boolean strictRotations = true;

    public AntiBedTrap() {
        super("AntiBedTrap", Category.COMBAT);
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
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        // Base integer location coordinates matching the player's current body space
        BlockPos playerPos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));

        // SCAN MATRIX: Sweeps a strict tactical cube perimeter enclosing your feet, waist, and head boxes [5b5t.org]
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);

                    // Verify reach boundaries mathematically against your actual player eye height position coordinates
                    if (mc.player.getDistanceSqToCenter(checkPos) > range * range) {
                        continue;
                    }

                    // Look for vanilla bed blocks active inside the block chunk grids [5b5t.org]
                    Block targetBlock = mc.world.getBlockState(checkPos).getBlock();
                    if (targetBlock == Blocks.BED) {
                        executeBedDestruction(checkPos);
                        return; // Process a single target per tick sequence pass to preserve data sync lines [5b5t.org]
                    }
                }
            }
        }
    }

    private void executeBedDestruction(BlockPos bedPos) {
        int pickaxeSlot = findPickaxeSlot();
        int originalSlot = mc.player.inventory.currentItem;

        // 1. Silent Rotation alignment targeting the bed center layout array
        if (strictRotations) {
            lookAtVector(new Vec3d(bedPos.getX() + 0.5, bedPos.getY() + 0.5, bedPos.getZ() + 0.5));
        }

        // 2. Ghost-Hand Tool Handshake Switch
        if (autoWeaponSwap && pickaxeSlot != -1) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(pickaxeSlot));
        }

        // 3. FAST PACKET DIGGING BREAK SEQUENCE
        // Beds have extremely low hardness values (0.2). Sending the START and STOP mining packets
        // back-to-back while holding an efficiency pickaxe instantly deletes the bed in a single frame tick! [5b5t.org]
        mc.player.connection.sendPacket(new CPacketPlayerDigging(
                CPacketPlayerDigging.Action.START_DESTROY_BLOCK, bedPos, EnumFacing.UP));
        mc.player.connection.sendPacket(new CPacketPlayerDigging(
                CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, bedPos, EnumFacing.UP));

        mc.player.swingArm(EnumHand.MAIN_HAND);

        // 4. Return network indicator selector back onto your primary weapon index
        if (autoWeaponSwap && pickaxeSlot != -1) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
        }
    }

    private int findPickaxeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemPickaxe) {
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

    @Override
    protected void onUpdate() {}
}
