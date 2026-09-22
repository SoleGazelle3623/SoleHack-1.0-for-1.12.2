package com.yourwebsitespace.solehack.modules.movement;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.common.MinecraftForge;

public class Velocity extends Module {

    public static Velocity INSTANCE;
    private final Minecraft mc = Minecraft.getMinecraft();

    // Knockback settings (0.0f = 0% velocity taken)
    public float horizontal = 0.0f;
    public float vertical = 0.0f;

    // NoPush settings
    public boolean noPush = true;      // General entity push
    public boolean blocks = true;      // Block collision push (e.g. inside blocks)
    public boolean liquids = true;     // Water / Lava flow push

    public Velocity() {
        super("Velocity", Category.MOVEMENT);
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

    public boolean onPacketReceive(Object packet) {
        if (!this.isEnabled() || mc.player == null) return false;

        if (packet instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocityPacket = (SPacketEntityVelocity) packet;
            if (velocityPacket.getEntityID() == mc.player.getEntityId()) {
                if (horizontal == 0.0f && vertical == 0.0f) {
                    return true;
                }
            }
        }

        if (packet instanceof SPacketExplosion) {
            if (horizontal == 0.0f && vertical == 0.0f) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onUpdate() {}
}