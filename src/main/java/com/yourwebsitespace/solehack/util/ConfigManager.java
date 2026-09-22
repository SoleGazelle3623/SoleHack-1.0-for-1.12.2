package com.yourwebsitespace.solehack.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yourwebsitespace.solehack.Module;
import com.yourwebsitespace.solehack.ModuleManager;
import com.yourwebsitespace.solehack.MyMod;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File(Minecraft.getMinecraft().gameDir, "SoleHack");

    public static void saveConfig() {
        saveConfig("settings");
    }

    public static void loadConfig() {
        loadConfig("settings");
    }

    public static void saveConfig(String configName) {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            return;
        }

        File targetFile = new File(CONFIG_DIR, configName + ".json");
        JsonObject json = new JsonObject();
        JsonObject modulesJson = new JsonObject();

        for (Module module : ModuleManager.getModules()) {
            JsonObject modObj = new JsonObject();
            modObj.addProperty("enabled", module.isEnabled());
            modObj.addProperty("key", module.getKey());
            modulesJson.add(module.getName(), modObj);
        }
        json.add("Modules", modulesJson);

        try (FileWriter writer = new FileWriter(targetFile)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            MyMod.LOGGER.error("Failed to save configuration: {}", configName, e);
        }
    }

    public static void loadConfig(String configName) {
        File targetFile = new File(CONFIG_DIR, configName + ".json");
        if (!targetFile.exists()) return;

        try (FileReader reader = new FileReader(targetFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

            if (json.has("Modules")) {
                JsonObject modulesJson = json.getAsJsonObject("Modules");
                for (Module module : ModuleManager.getModules()) {
                    if (modulesJson.has(module.getName())) {
                        JsonObject modObj = modulesJson.getAsJsonObject(module.getName());

                        if (modObj.has("enabled")) {
                            module.setEnabled(modObj.get("enabled").getAsBoolean());
                        }

                        if (modObj.has("key")) {
                            module.setKey(modObj.get("key").getAsInt());
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyMod.LOGGER.error("Failed to load configuration: {}", configName, e);
        }
    }
}