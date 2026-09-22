package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Nametag extends Module {

    public Nametag() {
        super("Nametag", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        if (event.getEntity() instanceof EntityPlayerSP) return; // skip yourself

        EntityPlayer target = (EntityPlayer) event.getEntity();
        Minecraft mc = Minecraft.getMinecraft();

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        int armor = target.getTotalArmorValue(); // 0-20 scale, vanilla armor points
        ItemStack offhand = target.getHeldItemOffhand();
        String offhandName = offhand.isEmpty() ? "Empty" : offhand.getDisplayName();

        String line = String.format("HP: %.1f/%.1f  Armor: %d  Offhand: %s",
                health, maxHealth, armor, offhandName);

        double x = event.getX();
        double y = event.getY() + target.height + 0.5; // just above the vanilla nametag
        double z = event.getZ();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        GlStateManager.scale(-0.025f, -0.025f, 0.025f);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();

        int width = mc.fontRenderer.getStringWidth(line);
        mc.fontRenderer.drawString(line, -width / 2, 0, 0xFFFFFF);

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}