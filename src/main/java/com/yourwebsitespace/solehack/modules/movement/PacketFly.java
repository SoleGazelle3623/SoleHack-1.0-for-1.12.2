package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PacketFly extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    // 5b5t Strict Anti-Cheat Compliant Velocity Scales
    // Keeping speed below 0.05-0.07 bounds provides stable, slow packet flight bypasses
    public double horizontalSpeed = 0.045;
    public double verticalSpeed = 0.035;

    public PacketFly() {
        super("PacketFly", Category.MOVEMENT);
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

        // Freeze all vanilla gravity updates instantly to prevent falling flags
        mc.player.motionX = 0.0;
        mc.player.motionY = 0.0;
        mc.player.motionZ = 0.0;

        double radYaw = Math.toRadians(mc.player.rotationYaw);
        double outX = 0.0;
        double outY = 0.0;
        double outZ = 0.0;

        // 1. Process directional movement inputs with tight horizontal constraints
        if (mc.gameSettings.keyBindForward.isKeyDown()) {
            outX -= Math.sin(radYaw) * horizontalSpeed;
            outZ += Math.cos(radYaw) * horizontalSpeed;
        }
        if (mc.gameSettings.keyBindBack.isKeyDown()) {
            outX += Math.sin(radYaw) * horizontalSpeed;
            outZ -= Math.cos(radYaw) * horizontalSpeed;
        }
        if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            outX -= Math.cos(radYaw) * horizontalSpeed;
            outZ -= Math.sin(radYaw) * horizontalSpeed;
        }
        if (mc.gameSettings.keyBindRight.isKeyDown()) {
            outX += Math.cos(radYaw) * horizontalSpeed;
            outZ += Math.sin(radYaw) * horizontalSpeed;
        }

        // 2. Process vertical ascension / descension inputs (Space / Shift)
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            outY += verticalSpeed;
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            outY -= verticalSpeed;
        }

        // 3. PACKET-SPOOFING FLUID BYPASS LAYER
        // To counter strict server-side transaction checks, we send alternating micro-offsets
        // down the pipeline before updating our position to fool the position tracking engine.
        double posX = mc.player.posX + outX;
        double posY = mc.player.posY + outY;
        double posZ = mc.player.posZ + outZ;

        // Spoof a minor server-side offset confirmation packet sequence directly down the network wire
        mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, posY - 0.04, posZ, false));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, posY, posZ, true));

        // Physically update the local client player entity location boundaries to match
        mc.player.setPosition(posX, posY, posZ);
    }

    @Override
    protected void onUpdate() {}
}
