package com.yourwebsitespace.solehack.modules.misc;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.network.Packet;

public class PacketLimiter extends Module {

    public static PacketLimiter INSTANCE;

    // Configurable limit
    private final int maxPacketsPerSecond = 50;
    private int packetCounter = 0;
    private long lastResetTime = 0;

    public PacketLimiter() {
        super("PacketLimiter", Category.MISC);
        INSTANCE = this;
    }

    @Override
    protected void onUpdate() {
        if (!isEnabled()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= 1000) {
            packetCounter = 0;
            lastResetTime = currentTime;
        }
    }

    public boolean onPacketSend(Packet<?> packet) {
        if (!isEnabled()) return true;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= 1000) {
            packetCounter = 0;
            lastResetTime = currentTime;
        }

        packetCounter++;

        // Drop packets if they exceed the max threshold per second
        return packetCounter <= maxPacketsPerSecond;
    }
}