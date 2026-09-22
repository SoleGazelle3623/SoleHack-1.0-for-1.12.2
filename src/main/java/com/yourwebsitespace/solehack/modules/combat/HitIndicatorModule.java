package com.yourwebsitespace.solehack.modules.combat;

import com.yourwebsitespace.solehack.Category;
import com.yourwebsitespace.solehack.Module;

public class HitIndicatorModule extends Module {
    public HitIndicatorModule() {
        super("Hit Indicator", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        // hook into attack event to play a sound/flash on successful hit
    }

    @Override
    protected void onUpdate() {

    }

    @Override
    protected void onDisable() {
        // unhook
    }
}