package com.yourwebsitespace.solehack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class ClientEventHandler {

    private final Map<String, Boolean> wasDown = new HashMap<>();

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !(event.getGui() instanceof CustomMainMenu)) {
            event.setGui(new CustomMainMenu());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        while (KeyBindings.openGui.isPressed()) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ConfigScreen(null));
            }
        }

        // Per-module keybind toggling (skip while typing in a GUI)
        if (mc.currentScreen == null) {
            for (Module module : ModuleManager.getAll()) {
                int key = module.getKeyCode();
                if (key == -1) continue;

                boolean down = Keyboard.isKeyDown(key);
                boolean prev = wasDown.getOrDefault(module.getName(), false);

                if (down && !prev) {
                    module.setEnabled(!module.isEnabled());
                }
                wasDown.put(module.getName(), down);
            }
        }
    }
}