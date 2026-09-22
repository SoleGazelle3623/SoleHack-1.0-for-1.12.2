package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

public class TotemPopVisual extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    public static TotemPopVisual INSTANCE;

    private final Map<String, Integer> popCounts = new HashMap<>();

    public TotemPopVisual() {
        super("TotemPopVisual", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        popCounts.clear();
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    // Intercept incoming packets client-side to detect totem pops (OpCode 35)
    public void onPacketReceive(Object packet) {
        if (!this.isEnabled() || mc.world == null) return;

        if (packet instanceof SPacketEntityStatus) {
            SPacketEntityStatus status = (SPacketEntityStatus) packet;
            if (status.getOpCode() == 35) {
                Entity entity = status.getEntity(mc.world);
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;

                    String name = player.getName();
                    int count = popCounts.getOrDefault(name, 0) + 1;
                    popCounts.put(name, count);

                    // Client-side chat notification
                    if (mc.player != null) {
                        mc.player.sendMessage(new TextComponentString(
                                "\u00a7c[TotemPop] \u00a7f" + name + " popped \u00a7e#" + count + " totem, LOL."
                        ));
                    }
                }
            }
        }
    }

    @Override
    protected void onUpdate() {}
}