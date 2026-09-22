package com.yourwebsitespace.solehack.modules.render;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class FullBright extends Module {

    private float savedGamma;

    public FullBright() {
        super("FullBright", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        savedGamma = settings.gammaSetting;
        settings.gammaSetting = 1000.0F; // vanilla caps the slider at 1.0, but the field itself accepts much higher
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        Minecraft.getMinecraft().gameSettings.gammaSetting = savedGamma;
    }
}