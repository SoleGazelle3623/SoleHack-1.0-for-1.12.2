package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ElytraFly extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    // Configurations
    private final double horizontalSpeed = 2.6; // Custom horizontal speed multiplier
    private boolean isDoubleJumping = false;
    private boolean wasJumpPressed = false;
    private int jumpTicks = 0;

    public ElytraFly() {
        super("ElytraFly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        resetFlightState();
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        resetFlightState();
    }

    private void resetFlightState() {
        isDoubleJumping = false;
        wasJumpPressed = false;
        jumpTicks = 0;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!this.isEnabled() || mc.player == null) return;
        EntityPlayer player = mc.player;

        // Only run code on the client side local player
        if (player.world.isRemote && player.equals(mc.player)) {

            // 1. Reset state when touching the ground
            if (player.onGround) {
                isDoubleJumping = false;
            }

            // 2. Double-jump detection logic
            boolean isJumpPressed = mc.gameSettings.keyBindJump.isKeyDown();

            if (jumpTicks > 0) {
                jumpTicks--;
            }

            if (isJumpPressed && !wasJumpPressed) { // Key pressed event
                if (!player.onGround && !isDoubleJumping) {
                    if (jumpTicks > 0) {
                        isDoubleJumping = true; // Trigger double jump flight
                    } else {
                        jumpTicks = 7; // Window of ticks to press jump again
                    }
                }
            }
            wasJumpPressed = isJumpPressed;

            // 3. Movement execution during double jump flight
            if (isDoubleJumping) {
                // Force Elytra fall-flying state packet flag
                if (!player.isElytraFlying()) {
                    player.setAir(7);
                }

                // Vertical control
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    player.motionY += 0.08;
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    player.motionY -= 0.08;
                } else {
                    player.motionY = 0.0; // Altitude lock hover
                }

                // Horizontal speed enhancement based on player's current look direction
                float moveForward = ((net.minecraft.client.entity.EntityPlayerSP) player).movementInput.moveForward;
                float moveStrafe = ((net.minecraft.client.entity.EntityPlayerSP) player).movementInput.moveStrafe;

                if (moveForward != 0.0F || moveStrafe != 0.0F) {
                    // Combine angles to match directional movement input
                    float yaw = player.rotationYaw;
                    if (moveForward < 0.0F) yaw += 180.0F;
                    if (moveStrafe > 0.0F) yaw -= 90.0F * (moveForward < 0.0F ? -0.5F : (moveForward > 0.0F ? 0.5F : 1.0F));
                    if (moveStrafe < 0.0F) yaw += 90.0F * (moveForward < 0.0F ? -0.5F : (moveForward > 0.0F ? 0.5F : 1.0F));

                    double radians = Math.toRadians(yaw);
                    player.motionX = -MathHelper.sin((float) radians) * horizontalSpeed;
                    player.motionZ = MathHelper.cos((float) radians) * horizontalSpeed;
                } else {
                    // Instantly halt horizontal drifting when inputs are released
                    player.motionX = 0.0;
                    player.motionZ = 0.0;
                }
            }
        }
    }
}
