package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
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

public class Surround extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Optimized Speed Profile settings
    public int blocksPerTick = 2;       // Maximum blocks to place in 1 tick (prevents server packet kicks)
    public boolean strictRotations = true;
    public boolean autoDisable = true;  // Automatically turns off once the surrounding cage is full

    public Surround() {
        super("Surround", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);

        // AUTO-CENTER LOGIC: Executes immediately upon enabling the module
        if (mc.player != null && mc.world != null) {
            autoCenter();
        }
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private void autoCenter() {
        // Find the absolute integer base position of the block the player is standing on
        double centerX = MathHelper.floor(mc.player.posX) + 0.5;
        double centerZ = MathHelper.floor(mc.player.posZ) + 0.5;

        // Keep the exact current vertical position to avoid illegal height flags
        double posY = mc.player.posY;

        // Spoof the center alignment position to the server instantly
        mc.player.connection.sendPacket(new CPacketPlayer.Position(centerX, posY, centerZ, mc.player.onGround));

        // Snap the local player position to match the centered coordinates
        mc.player.setPosition(centerX, posY, centerZ);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        int obsidianSlot = findObsidianSlot();
        if (obsidianSlot == -1) {
            if (autoDisable) this.setEnabled(false);
            return; // Out of obsidian blocks
        }

        // Get the player's exact underlying floor block coordinate position
        BlockPos playerPos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));

        // Define the 4 cardinal directions enclosing the feet layer matrix space
        BlockPos[] surroundTargets = new BlockPos[] {
                playerPos.north(),
                playerPos.south(),
                playerPos.east(),
                playerPos.west()
        };

        int blocksPlacedThisTick = 0;
        int currentSlot = mc.player.inventory.currentItem;

        for (BlockPos targetPos : surroundTargets) {
            if (blocksPlacedThisTick >= blocksPerTick) break;

            // Check if the block is already filled or occupied
            if (!mc.world.getBlockState(targetPos).getMaterial().isReplaceable()) {
                continue;
            }

            // Find a valid solid adjacent support block face to attach our block against
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

            // Fallback: If no neighbors exist, search directly beneath the target position (floor anchor)
            if (neighbor == null) {
                BlockPos below = targetPos.down();
                if (!mc.world.isAirBlock(below)) {
                    neighbor = below;
                    side = EnumFacing.UP;
                }
            }

            if (neighbor != null && side != null) {
                // 1. Silent Rotation alignment targeting the neighboring attachment vector face center
                if (strictRotations) {
                    lookAtBlockFace(neighbor, side);
                }

                // 2. Ghost hand hotbar swap packet sequence
                mc.player.connection.sendPacket(new CPacketHeldItemChange(obsidianSlot));

                // 3. Dispatch the placement use packet execution stream down the pipeline wire
                Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                        neighbor, side, EnumHand.MAIN_HAND, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z));
                mc.player.swingArm(EnumHand.MAIN_HAND);

                // 4. Return to original active inventory held asset index key item
                mc.player.connection.sendPacket(new CPacketHeldItemChange(currentSlot));

                blocksPlacedThisTick++;
            }
        }

        // If the module finished wrapping the entire protective cage, auto-toggle off to save processing cycles
        if (blocksPlacedThisTick == 0 && autoDisable) {
            this.setEnabled(false);
        }
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                if (((ItemBlock) stack.getItem()).getBlock() == Blocks.OBSIDIAN) {
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
