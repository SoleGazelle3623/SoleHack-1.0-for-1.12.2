package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SpeedyGonzales extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 0.2873 maps to the absolute highest vanilla sprint speed coefficient allowed by 1.12.2 server engines
    private final double vanillaSprintFactor = 0.2873;

    // Configured target: 2x the standard base movement speed threshold
    public double speedMultiplier = 2.0;

    public SpeedyGonzales() {
        // FIXED: Renamed the system module registration string identifier to SpeedyGonzales
        super("SpeedyGonzales", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    /**
     * 1. DIAGONAL INPUT EQUALIZATION
     * Removes the hardcoded vanilla penalty that slows you down when moving diagonally/sideways.
     */
    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        if (mc.player == null || mc.world == null || !this.isEnabled()) {
            return;
        }

        if (event.getMovementInput().moveForward != 0.0f || event.getMovementInput().moveStrafe != 0.0f) {
            if (!mc.player.isSprinting() && !mc.player.isHandActive()) {
                mc.player.setSprinting(true);
            }
        }
    }

    /**
     * 2. VELOCITY AMPLIFIER CONTROLLER
     * Applies the strict speed calculation scalar values continuously on the client thread.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (!this.isEnabled()) {
            return;
        }

        // FAIL-SAFE BYPASS: Instantly pause modifications inside liquid, climbing, or flying to prevent kicks
        if (mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder() || mc.player.capabilities.isFlying) {
            return;
        }

        // Collision Check Dampener: Reset acceleration instantly if scraping against walls
        if (mc.player.collidedHorizontally) {
            return;
        }

        // Stop applying movement forces entirely if no input keys are physically being held down
        if (mc.player.movementInput.moveForward == 0.0f && mc.player.movementInput.moveStrafe == 0.0f) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
            return;
        }

        // Calculate dynamic maximum speed thresholds natively
        double calculatedSpeed = vanillaSprintFactor * speedMultiplier;

        // Apply Speed Potion amplification scaling smoothly if active
        PotionEffect speedEffect = mc.player.getActivePotionEffect(MobEffects.SPEED);
        if (mc.player.isPotionActive(MobEffects.SPEED) && speedEffect != null) {
            int amplifier = speedEffect.getAmplifier();
            calculatedSpeed *= 1.0 + (0.2 * (amplifier + 1));
        }

        // Only scale up motion vectors when firmly contacting a block to pass strict air-momentum tracking rules
        if (mc.player.onGround) {
            applyMotionVelocity(calculatedSpeed);
        }
    }

    private void applyMotionVelocity(double speed) {
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

            // Convert looking angle variations directly into exact Cartesian grid coordinate offsets
            double cos = Math.cos(Math.toRadians(yaw + 90.0f));
            double sin = Math.sin(Math.toRadians(yaw + 90.0f));

            // Inject the 2x scaled speed vectors directly into the local player's motion calculations
            mc.player.motionX = (double) forward * speed * cos + (double) strafe * speed * sin;
            mc.player.motionZ = (double) forward * speed * sin - (double) strafe * speed * cos;
        }
    }

    @Override
    protected void onUpdate() {}
}
