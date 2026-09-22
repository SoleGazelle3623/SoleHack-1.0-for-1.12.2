package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoScaffold extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Anti-Cheat Profiling
    public boolean strictRotations = true;
    public boolean safeWalk = true;       // Prevents slipping off edges while packets process
    public boolean tower = true;          // Fast vertical pillar support

    public AutoScaffold() {
        super("AutoScaffold", Category.MOVEMENT);
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

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) {
            return; // Out of construction materials
        }

        // 1. SAFEWALK PREDICTION LOGIC: Calculate your next anticipated block boundary step
        double futureX = mc.player.posX + (mc.player.motionX * 1.5);
        double futureZ = mc.player.posZ + (mc.player.motionZ * 1.5);

        BlockPos playerFeetPos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY - 1.0), Math.floor(mc.player.posZ));
        BlockPos predictivePos = new BlockPos(Math.floor(futureX), Math.floor(mc.player.posY - 1.0), Math.floor(futureZ));

        // Target the predictive gap block if moving quickly, otherwise default to current feet
        BlockPos targetPos = mc.world.isAirBlock(predictivePos) ? predictivePos : playerFeetPos;

        if (!mc.world.isAirBlock(targetPos)) {
            return;
        }

        // 2. EQUALIZATION DAMPER: Slow down slightly over open gaps to prevent anti-cheat speed violations
        if (safeWalk && mc.player.onGround && mc.world.isAirBlock(targetPos)) {
            mc.player.motionX *= 0.65;
            mc.player.motionZ *= 0.65;
        }

        // Scan neighboring faces to identify a solid attachment anchor point
        BlockPos neighbor = null;
        EnumFacing side = null;

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offsetPos = targetPos.offset(facing);
            if (!mc.world.isAirBlock(offsetPos)) {
                neighbor = offsetPos;
                side = facing.getOpposite();
                break;
            }
        }

        // Downward vertical floor fallback anchor check
        if (neighbor == null) {
            BlockPos below = targetPos.down();
            if (!mc.world.isAirBlock(below)) {
                neighbor = below;
                side = EnumFacing.UP;
            }
        }

        if (neighbor != null) {
            int originalSlot = mc.player.inventory.currentItem;

            // 3. STRICT PACKET ROTATIONS: Aim silently at the center block face vector
            if (strictRotations) {
                lookAtBlockFace(neighbor, side);
            }

            // Tower Logic: Fast climbing vertical pillar mechanics
            if (tower && mc.gameSettings.keyBindJump.isKeyDown() && mc.player.moveForward == 0.0f && mc.player.moveVertical == 0.0f) {
                mc.player.motionX = 0.0;
                mc.player.motionZ = 0.0;
                mc.player.motionY = 0.41999998688698; // Precise vanilla jumping metric double
            }

            // Execute ghost-hand swap sequence
            mc.player.connection.sendPacket(new CPacketHeldItemChange(blockSlot));

            // Interact precisely using offset hit calculations to clear strict raytrace checks
            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    neighbor, side, EnumHand.MAIN_HAND, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z));
            mc.player.swingArm(EnumHand.MAIN_HAND);

            // Revert back to original hotbar held slot instantly
            mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
        }
    }

    private int findBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                // Avoid placing blocks that break movement structures or trigger accidental GUI screens
                if (block != Blocks.AIR && block != Blocks.TNT && block != Blocks.CRAFTING_TABLE
                        && block != Blocks.CHEST && block != Blocks.TRAPPED_CHEST && block != Blocks.FURNACE) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void lookAtBlockFace(BlockPos pos, EnumFacing facing) {
        Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getDirectionVec()).scale(0.5));

        double diffX = hitVec.x - mc.player.posX;
        double diffY = hitVec.y - (mc.player.posY + mc.player.getEyeHeight());
        double diffZ = hitVec.z - mc.player.posZ;
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
