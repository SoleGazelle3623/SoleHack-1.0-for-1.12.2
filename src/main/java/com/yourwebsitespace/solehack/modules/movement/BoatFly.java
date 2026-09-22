package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BoatFly extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Settings
    public double speed = 2.5;
    public double verticalSpeed = 0.5;
    public boolean glide = true;
    public boolean cancelGravity = true;

    public BoatFly() {
        super("BoatFly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        // Check if player is sitting in a boat
        if (mc.player.getRidingEntity() instanceof EntityBoat) {
            EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();

            double motionX = 0.0;
            double motionY = 0.0;
            double motionZ = 0.0;

            // Handle Vertical Movement
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = verticalSpeed;
            } else if (mc.gameSettings.keyBindSprint.isKeyDown()) {
                motionY = -verticalSpeed;
            } else if (glide) {
                motionY = -0.04; // Slow hover drop
            } else if (!cancelGravity) {
                motionY = boat.motionY;
            }

            // Handle Directional Movement (Forward, Strafe, Yaw)
            float forward = mc.player.movementInput.moveForward;
            float strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;

            // Sync boat orientation with player rotation so boat turns smoothly
            boat.rotationYaw = mc.player.rotationYaw;

            if (forward != 0.0f || strafe != 0.0f) {
                if (forward != 0.0f) {
                    if (strafe > 0.0f) {
                        yaw += (forward > 0.0f ? -45 : 45);
                    } else if (strafe < 0.0f) {
                        yaw += (forward > 0.0f ? 45 : -45);
                    }
                    strafe = 0.0f;
                    if (forward > 0.0f) {
                        forward = 1.0f;
                    } else if (forward < 0.0f) {
                        forward = -1.0f;
                    }
                }

                double rad = Math.toRadians(yaw + 90.0f);
                motionX = (forward * speed * Math.cos(rad)) + (strafe * speed * Math.sin(rad));
                motionZ = (forward * speed * Math.sin(rad)) - (strafe * speed * Math.cos(rad));
            }

            // Apply direct motion vectors to the vehicle entity
            boat.motionX = motionX;
            boat.motionY = motionY;
            boat.motionZ = motionZ;

            // Optional arm swing visual when navigating
            if (forward != 0.0f || strafe != 0.0f || mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }
}