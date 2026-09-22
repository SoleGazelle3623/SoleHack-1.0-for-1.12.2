package com.yourwebsitespace.solehack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static Module getModuleByName(String name) {
        // Replace 'modules' with your actual list name (e.g., moduleList, modulesArray, etc.)
        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public static void register(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModulesByCategory(Category category) {
        return MODULES.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public static List<Module> getAll() {
        return MODULES;
    }

    public static List<Module> getModules() {
        return MODULES;
    }
}