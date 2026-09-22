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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HoleFill extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public double range = 5.0;
    public boolean strictRotations = true;
    public boolean smart = true; // Only fill holes near targets

    public HoleFill() {
        super("HoleFill", Category.COMBAT);
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
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) return;
        if (!this.isEnabled()) return;

        int obsidianSlot = findObsidianSlot();
        if (obsidianSlot == -1) return;

        BlockPos targetHole = findBestHole();
        if (targetHole != null) {
            fillHole(targetHole, obsidianSlot);
        }
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == Blocks.OBSIDIAN) {
                return i;
            }
        }
        return -1;
    }

    private BlockPos findBestHole() {
        List<BlockPos> holes = new ArrayList<>();
        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        int rad = (int) Math.ceil(range);
        for (int x = -rad; x <= rad; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -rad; z <= rad; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (mc.player.getDistanceSq(pos) > range * range) continue;

                    if (isHole(pos)) {
                        if (smart) {
                            boolean enemyNearby = mc.world.playerEntities.stream()
                                    .anyMatch(p -> p != mc.player && !p.isDead && p.getDistanceSq(pos) <= 4.0);
                            if (!enemyNearby) continue;
                        }
                        holes.add(pos);
                    }
                }
            }
        }

        return holes.stream()
                .min(Comparator.comparingDouble(pos -> mc.player.getDistanceSq(pos)))
                .orElse(null);
    }

    private boolean isHole(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) return false;
        if (!mc.world.isAirBlock(pos.up())) return false;

        BlockPos down = pos.down();
        return (mc.world.getBlockState(down).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(down).getBlock() == Blocks.BEDROCK) &&
                (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK) &&
                (mc.world.getBlockState(pos.south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK) &&
                (mc.world.getBlockState(pos.east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK) &&
                (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK);
    }

    private void fillHole(BlockPos pos, int slot) {
        int currentItem = mc.player.inventory.currentItem;

        if (strictRotations) {
            lookAtVector(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        }

        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos.down(), EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f));
        mc.player.connection.sendPacket(new CPacketHeldItemChange(currentItem));
        mc.player.swingArm(EnumHand.MAIN_HAND);
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