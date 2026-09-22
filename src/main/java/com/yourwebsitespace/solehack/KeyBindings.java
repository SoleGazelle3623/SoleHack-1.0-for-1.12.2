package com.yourwebsitespace.solehack;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    public static KeyBinding openGui;

    public static void init() {
        openGui = new KeyBinding("key.solehack.opengui", Keyboard.KEY_RSHIFT, "key.categories.solehack");
        ClientRegistry.registerKeyBinding(openGui);
    }
}