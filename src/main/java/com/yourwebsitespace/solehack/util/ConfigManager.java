package com.yourwebsitespace.solehack.util;

import com.yourwebsitespace.solehack.Module;
import com.yourwebsitespace.solehack.ModuleManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private static final File CONFIG_DIR = new File(net.minecraft.client.Minecraft.getMinecraft().gameDir, "SoleHack/configs");

    // Updated to accept a config name string
    public static void saveConfig(String name) {
        if (!CONFIG_DIR.exists()) {
            boolean ignored = CONFIG_DIR.mkdirs();
        }
        File configFile = new File(CONFIG_DIR, name + ".json");

        JsonObject json = new JsonObject();
        JsonArray modulesArray = new JsonArray();

        for (Module module : ModuleManager.getModules()) {
            JsonObject modObj = new JsonObject();
            modObj.addProperty("name", module.getName());
            modObj.addProperty("enabled", module.isEnabled());
            modObj.addProperty("key", module.getKey());

            modulesArray.add(modObj);
        }

        json.add("modules", modulesArray);

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json.toString());
        } catch (IOException e) {
            com.yourwebsitespace.solehack.MyMod.LOGGER.error("Failed to save config", e);
        }
    }

    // Updated to accept a config name string
    public static void loadConfig(String name) {
        File configFile = new File(CONFIG_DIR, name + ".json");
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            if (json.has("modules")) {
                JsonArray modulesArray = json.getAsJsonArray("modules");

                modulesArray.forEach(element -> {
                    JsonObject modObj = element.getAsJsonObject();
                    String modName = modObj.get("name").getAsString();
                    boolean enabled = modObj.get("enabled").getAsBoolean();
                    int key = modObj.has("key") ? modObj.get("key").getAsInt() : 0;

                    for (Module module : ModuleManager.getModules()) {
                        if (module.getName().equalsIgnoreCase(modName)) {
                            if (module.isEnabled() != enabled) {
                                module.setEnabled(enabled);
                            }
                            module.setKey(key);
                        }
                    }
                });
            }
        } catch (Exception e) {
            com.yourwebsitespace.solehack.MyMod.LOGGER.error("Failed to load config", e);
        }
    }
}