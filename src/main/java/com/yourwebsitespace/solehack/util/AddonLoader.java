package com.yourwebsitespace.solehack.util;

import com.yourwebsitespace.solehack.MyMod;
import com.yourwebsitespace.solehack.ModuleManager;
import com.yourwebsitespace.solehack.api.ISoleHackAddon;
import com.yourwebsitespace.solehack.Module;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AddonLoader {

    public static final List<ISoleHackAddon> LOADED_ADDONS = new ArrayList<>();

    public static void loadAddons() {
        File addonsDir = new File(Minecraft.getMinecraft().gameDir, "SoleHack/addons");
        if (!addonsDir.exists() && !addonsDir.mkdirs()) return;

        File[] files = addonsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });

        if (files == null) return;

        for (File file : files) {
            try (JarFile jar = new JarFile(file)) {
                URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, AddonLoader.class.getClassLoader());

                Manifest manifest = jar.getManifest();
                if (manifest == null) continue;
                String mainClass = manifest.getMainAttributes().getValue("Addon-Main");
                if (mainClass == null) continue;

                Class<?> cls = Class.forName(mainClass, true, loader);
                if (ISoleHackAddon.class.isAssignableFrom(cls)) {
                    ISoleHackAddon addon = (ISoleHackAddon) cls.getDeclaredConstructor().newInstance();

                    for (Module mod : addon.getModules()) {
                        ModuleManager.getModules().add(mod);
                    }
                    LOADED_ADDONS.add(addon);
                    MyMod.LOGGER.info("Successfully loaded addon: {} by {}", addon.getName(), addon.getAuthor());
                }
            } catch (Exception e) {
                MyMod.LOGGER.error("Failed to load addon jar: {}", file.getName(), e);
            }
        }
    }
}