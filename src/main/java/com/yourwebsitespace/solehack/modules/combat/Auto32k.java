package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Auto32k extends Module {

    public static Auto32k INSTANCE;
    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean isPlacing = false;

    public Auto32k() {
        super("Auto32k", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        isPlacing = false;

        if (mc.player == null || mc.world == null) return;

        int hopperSlot = findBlockSlot(Blocks.HOPPER);
        int shulkerSlot = findShulkerSlot();

        if (hopperSlot == -1 || shulkerSlot == -1) {
            this.setEnabled(false);
            return;
        }

        RayTraceResult ray = mc.objectMouseOver;
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            this.setEnabled(false);
            return;
        }

        BlockPos targetPos = ray.getBlockPos();
        EnumFacing side = ray.sideHit;

        BlockPos hopperPos = targetPos.offset(side);
        BlockPos shulkerPos = hopperPos.up();

        if (!mc.world.getBlockState(hopperPos).getMaterial().isReplaceable() ||
                !mc.world.getBlockState(shulkerPos).getMaterial().isReplaceable()) {
            this.setEnabled(false);
            return;
        }

        // 1. Place Hopper
        placeBlock(hopperPos, hopperSlot, side.getOpposite());

        // 2. Place Shulker Box on top of Hopper
        placeBlock(shulkerPos, shulkerSlot, EnumFacing.DOWN);

        // 3. Open Hopper
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                hopperPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));

        isPlacing = true;
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        isPlacing = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || !isPlacing) return;

        // Container Check: Detects if a hopper inventory container is active
        if (mc.player.openContainer instanceof ContainerHopper) {
            ContainerHopper hopperContainer = (ContainerHopper) mc.player.openContainer;

            // Loop through the 5 hopper slots (slots 0 to 4)
            for (int i = 0; i < 5; i++) {
                ItemStack stack = hopperContainer.getSlot(i).getStack();

                if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
                    // SWAP slot i with Hotbar slot 0 (index 36 in openContainer indexing)
                    mc.playerController.windowClick(
                            hopperContainer.windowId,
                            i,
                            0, // Hotbar slot target index (0 = slot 1)
                            ClickType.SWAP,
                            mc.player
                    );

                    // Switch main hand to Hotbar slot 0
                    mc.player.inventory.currentItem = 0;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(0));

                    this.setEnabled(false);
                    return;
                }
            }
        }
    }

    private void placeBlock(BlockPos pos, int slot, EnumFacing facing) {
        int oldSlot = mc.player.inventory.currentItem;
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

        Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos.offset(facing), facing.getOpposite(), EnumHand.MAIN_HAND,
                (float) hitVec.x, (float) hitVec.y, (float) hitVec.z));

        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
    }

    private int findBlockSlot(Block block) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == block) {
                return i;
            }
        }
        return -1;
    }

    private int findShulkerSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block instanceof BlockShulkerBox) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    protected void onUpdate() {}
}