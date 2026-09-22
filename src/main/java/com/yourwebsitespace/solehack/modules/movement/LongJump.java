package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LongJump extends Module {

    protected static final Minecraft mc = Minecraft.getMinecraft();

    public LongJump() {
        super("LongJump", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isAirBorne && !mc.player.onGround) {
            mc.player.motionY += 0.05;

            double boost = 4.0;
            if (mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0) {
                float yaw = mc.player.rotationYaw;
                if (mc.player.movementInput.moveForward < 0) yaw += 180;
                if (mc.player.movementInput.moveStrafe > 0) yaw -= 90;
                if (mc.player.movementInput.moveStrafe < 0) yaw += 90;

                mc.player.motionX = Math.cos(Math.toRadians(yaw + 90)) * boost;
                mc.player.motionZ = Math.sin(Math.toRadians(yaw + 90)) * boost;
            }
        }
    }

    @Override
    protected void onUpdate() {

    }
}