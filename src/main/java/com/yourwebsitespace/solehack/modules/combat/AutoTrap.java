package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoTrap extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public double range = 5.0;
    public int blocksPerTick = 4;

    public AutoTrap() {
        super("AutoTrap", Category.COMBAT);
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

        EntityPlayer target = findTarget();
        if (target == null) {
            return;
        }

        int obbySlot = findObsidianSlot();
        if (obbySlot == -1) {
            return; // Out of obsidian in hotbar
        }

        int placed = 0;
        BlockPos targetPos = new BlockPos(target.getPositionVector());

        // Standard trap offset positions relative to the target player's feet/head
        BlockPos[] trapOffsets = {
                targetPos.add(0, 2, 0), // Head block cover
                targetPos.add(1, 1, 0),
                targetPos.add(-1, 1, 0),
                targetPos.add(0, 1, 1),
                targetPos.add(0, 1, -1)
        };

        int currentSlot = mc.player.inventory.currentItem;

        for (BlockPos pos : trapOffsets) {
            if (placed >= blocksPerTick) break;

            if (mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world, pos)) {
                if (mc.player.getDistanceSq(pos) <= range * range) {
                    placeBlock(pos, obbySlot, currentSlot);
                    placed++;
                }
            }
        }
    }

    private EntityPlayer findTarget() {
        EntityPlayer closest = null;
        double maxDist = range * range;

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == mc.player || player.isDead || player.getHealth() <= 0.0f) continue;
            double dist = mc.player.getDistanceSq(player);
            if (dist < maxDist) {
                maxDist = dist;
                closest = player;
            }
        }
        return closest;
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                if (((ItemBlock) stack.getItem()).getBlock() instanceof BlockObsidian) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void placeBlock(BlockPos pos, int obbySlot, int originalSlot) {
        // Quick packet hotbar switch
        mc.player.connection.sendPacket(new CPacketHeldItemChange(obbySlot));

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos.down(), EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f
        ));
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        // Revert hotbar index slot back
        mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
    }

    @Override
    protected void onUpdate() {}
}