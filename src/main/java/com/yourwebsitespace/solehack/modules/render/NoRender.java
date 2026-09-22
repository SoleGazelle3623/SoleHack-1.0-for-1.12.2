package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoRender extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    public static NoRender INSTANCE;

    // Toggleable sub-features for granular control
    public boolean explosions = true;
    public boolean fireOverlay = true;
    public boolean waterOverlay = true;
    public boolean blindness = true;
    public boolean totems = true;

    public NoRender() {
        super("NoRender", Category.RENDER);
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

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.world == null || event.phase != TickEvent.Phase.START) return;
        if (!this.isEnabled()) return;

        // Clear blindness potion effect client-side if enabled
        if (blindness && mc.player.isPotionActive(net.minecraft.init.MobEffects.BLINDNESS)) {
            mc.player.removePotionEffect(net.minecraft.init.MobEffects.BLINDNESS);
        }

        // Clear nausea/confusion effect client-side
        if (mc.player.isPotionActive(net.minecraft.init.MobEffects.NAUSEA)) {
            mc.player.removePotionEffect(net.minecraft.init.MobEffects.NAUSEA);
        }
    }

    @Override
    protected void onUpdate() {}
}