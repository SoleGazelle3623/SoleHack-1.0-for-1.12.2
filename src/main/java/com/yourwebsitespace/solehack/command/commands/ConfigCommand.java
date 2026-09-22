package com.yourwebsitespace.solehack.command.commands;

import com.yourwebsitespace.solehack.command.Command;
import com.yourwebsitespace.solehack.command.CommandManager;
import com.yourwebsitespace.solehack.util.ConfigManager;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "Save or load client configurations", ".config <load/save> <name>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            CommandManager.INSTANCE.sendClientMessage("§cUsage: " + getUsage());
            return;
        }

        String action = args[0].toLowerCase();
        String name = args[1];

        if (action.equals("load")) {
            ConfigManager.loadConfig(name);
            CommandManager.INSTANCE.sendClientMessage("§aSuccessfully loaded configuration: §f" + name);
        } else if (action.equals("save")) {
            ConfigManager.saveConfig(name);
            CommandManager.INSTANCE.sendClientMessage("§aSuccessfully saved configuration: §f" + name);
        } else {
            CommandManager.INSTANCE.sendClientMessage("§cUnknown action! Use §e'load' §cor §e'save'.");
        }
    }
}