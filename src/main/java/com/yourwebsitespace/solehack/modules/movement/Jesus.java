package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Jesus extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    public static Jesus INSTANCE;

    public Jesus() {
        super("Jesus", Category.MOVEMENT);
        INSTANCE = this;
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

        // Check if player is touching liquid and trying to move up
        if (isInLiquid() && !mc.player.isSneaking() && !mc.player.movementInput.jump) {
            mc.player.motionY = 0.11; // Slight upward push to stay on top of the fluid surface
            mc.player.onGround = true;
        }
    }

    private boolean isInLiquid() {
        if (mc.player == null) return false;
        double y = mc.player.posY - 0.05;
        for (int x = MathHelper.floor(mc.player.posX); x <= MathHelper.ceil(mc.player.posX); x++) {
            for (int z = MathHelper.floor(mc.player.posZ); z <= MathHelper.ceil(mc.player.posZ); z++) {
                BlockPos pos = new BlockPos(x, (int) y, z);
                if (mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onUpdate() {}
}