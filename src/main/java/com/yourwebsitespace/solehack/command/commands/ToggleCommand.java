package com.yourwebsitespace.solehack.command.commands;

import com.yourwebsitespace.solehack.Module;
import com.yourwebsitespace.solehack.ModuleManager;
import com.yourwebsitespace.solehack.command.Command;
import com.yourwebsitespace.solehack.command.CommandManager;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "Toggles a specific module on or off", "toggle <module>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandManager.INSTANCE.sendClientMessage("§cUsage: " + getUsage());
            return;
        }

        String moduleName = args[0];
        Module module = ModuleManager.getModuleByName(moduleName);

        if (module == null) {
            CommandManager.INSTANCE.sendClientMessage("§cModule '" + moduleName + "' not found!");
            return;
        }

        // Flips the current state (if your base class uses setEnabled/isEnabled)
        boolean newState = !module.isEnabled();
        module.setEnabled(newState);

        CommandManager.INSTANCE.sendClientMessage("§7Module §e" + module.getName() + " §7set to: " + (module.isEnabled() ? "§aENABLED" : "§cDISABLED"));
    }
}