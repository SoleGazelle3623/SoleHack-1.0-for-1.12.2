package com.yourwebsitespace.solehack.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.vector.Vector3f;

public class ProjectileHelper {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Vector3f[] calculateTrajectory(EntityPlayer player, Item item) {
        float partialTicks = mc.getRenderPartialTicks();

        // Initial positions (starting from player's eyes)
        double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks + player.getEyeHeight();
        double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        // View angles (pitch and yaw)
        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;

        // Calculate initial motion vector based on where the player is looking
        double motionX = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        double motionY = -MathHelper.sin(pitch * 0.017453292F);
        double motionZ = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);

        // Apply velocity multiplier based on item type
        float velocityMultiplier = 1.0F;
        float gravity = 0.03F;
        float airDrag = 0.99F;

        if (item instanceof ItemBow) {
            int useDuration = player.getItemInUseCount();
            float charge = (float)(72000 - useDuration) / 20.0F;
            charge = (charge * charge + charge * 2.0F) / 3.0F;

            if (charge > 1.0F) charge = 1.0F;
            if (charge < 0.1F) charge = 0.0F;

            velocityMultiplier = charge * 2.0F;
            gravity = 0.05F;
            airDrag = 0.99F;
        } else if (item instanceof ItemPotion || item instanceof ItemEnderPearl || item instanceof ItemEgg || item instanceof ItemSnowball) {
            velocityMultiplier = 1.5F;
            gravity = 0.03F;
            airDrag = 0.99F;
        }

        // Normalize motion vector to get direction
        double distance = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (distance != 0) {
            motionX /= distance;
            motionY /= distance;
            motionZ /= distance;
        }

        // Apply initial velocity multiplier
        motionX *= velocityMultiplier;
        motionY *= velocityMultiplier;
        motionZ *= velocityMultiplier;

        // Simulate path points
        int maxTicks = 500;
        Vector3f[] points = new Vector3f[maxTicks];

        for (int i = 0; i < maxTicks; i++) {
            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            // Apply gravity and air drag each tick
            motionY -= gravity;
            motionX *= airDrag;
            motionY *= airDrag;
            motionZ *= airDrag;

            if (posY < -1.0) {
                points[i] = null;
                break;
            }

            points[i] = new Vector3f((float) posX, (float) posY, (float) posZ);
        }
        return points;
    }
}