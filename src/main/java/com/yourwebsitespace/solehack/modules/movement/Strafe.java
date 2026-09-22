package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Strafe extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public double movementSpeedFactor = 0.2873;
    public boolean autoJump = true;

    public Strafe() {
        super("Strafe", Category.MOVEMENT);
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

        if (mc.player.movementInput.moveForward == 0.0f && mc.player.movementInput.moveStrafe == 0.0f) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
            return;
        }

        // Calculate base speed dynamically accounting for Speed potion effects
        double baseSpeed = 0.2873;
        net.minecraft.potion.PotionEffect speedEffect = mc.player.getActivePotionEffect(MobEffects.SPEED);
        if (speedEffect != null) {
            int amplifier = speedEffect.getAmplifier();
            baseSpeed *= 1.0 + (amplifier + 1) * 0.2;
        }

        // Handle safe auto-jump logic with strict anti-cheat multipliers
        if (autoJump && mc.player.onGround && (mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f)) {
            mc.player.jump();
            movementSpeedFactor = baseSpeed * 1.583; // Standard vanilla sprint-jump initial boost ratio
        } else {
            movementSpeedFactor = baseSpeed;
        }

        applyStrafeVelocity(movementSpeedFactor);
    }

    private void applyStrafeVelocity(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (forward == 0.0f && strafe == 0.0f) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
        } else {
            if (forward != 0.0f) {
                if (strafe > 0.0f) {
                    yaw += (float) (forward > 0.0f ? -45 : 45);
                } else if (strafe < 0.0f) {
                    yaw += (float) (forward > 0.0f ? 45 : -45);
                }
                strafe = 0.0f;
                if (forward > 0.0f) {
                    forward = 1.0f;
                } else if (forward < 0.0f) {
                    forward = -1.0f;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw + 90.0f));
            double sin = Math.sin(Math.toRadians(yaw + 90.0f));

            mc.player.motionX = (double) forward * speed * cos + (double) strafe * speed * sin;
            mc.player.motionZ = (double) forward * speed * sin - (double) strafe * speed * cos;
        }
    }

    @Override
    protected void onUpdate() {}
}