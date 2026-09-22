package com.yourwebsitespace.solehack.command.commands;

import com.yourwebsitespace.solehack.Module;
import com.yourwebsitespace.solehack.ModuleManager;
import com.yourwebsitespace.solehack.command.Command;
import com.yourwebsitespace.solehack.command.CommandManager;
import org.lwjgl.input.Keyboard;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Binds a module to a specific keyboard key", "bind <module> <key> (or 'none' to clear)");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            CommandManager.INSTANCE.sendClientMessage("§cUsage: " + getUsage());
            return;
        }

        String moduleName = args[0];
        String keyName = args[1];

        Module module = ModuleManager.getModuleByName(moduleName);
        if (module == null) {
            CommandManager.INSTANCE.sendClientMessage("§cModule '" + moduleName + "' not found!");
            return;
        }

        if (keyName.equalsIgnoreCase("none") || keyName.equalsIgnoreCase("clear")) {
            module.setKey(Keyboard.KEY_NONE);
            CommandManager.INSTANCE.sendClientMessage("§7Cleared keybind for §e" + module.getName());
            return;
        }

        int key = Keyboard.KEY_NONE;
        String upper = keyName.toUpperCase();

        // Fallback mapper for single letters and numbers which LWJGL's getKeyIndex often misses directly
        if (upper.length() == 1) {
            char c = upper.charAt(0);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                key = Keyboard.getKeyIndex("KEY_" + c);
            }
        }

        // If not matched, try standard lookup or prepend "KEY_" if missing
        if (key == Keyboard.KEY_NONE) {
            key = Keyboard.getKeyIndex(upper);
            if (key == Keyboard.KEY_NONE && !upper.startsWith("KEY_")) {
                key = Keyboard.getKeyIndex("KEY_" + upper);
            }
        }

        if (key == Keyboard.KEY_NONE) {
            CommandManager.INSTANCE.sendClientMessage("§cInvalid key name: '" + keyName + "'!");
            return;
        }

        module.setKey(key);
        CommandManager.INSTANCE.sendClientMessage("§7Bound §e" + module.getName() + " §7to key §a" + Keyboard.getKeyName(key) + "§7.");
    }
}