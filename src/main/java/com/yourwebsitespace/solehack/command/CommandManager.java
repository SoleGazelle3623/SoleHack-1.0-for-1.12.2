package com.yourwebsitespace.solehack.command;

import com.yourwebsitespace.solehack.command.commands.BindCommand;
import com.yourwebsitespace.solehack.command.commands.ToggleCommand;
import com.yourwebsitespace.solehack.command.commands.FriendCommand;
import com.yourwebsitespace.solehack.command.commands.ConfigCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final List<Command> commands = new ArrayList<>();
    private final String prefix = ".";
    public static CommandManager INSTANCE;

    public CommandManager() {
        INSTANCE = this;
        // Register your commands here
        commands.add(new ToggleCommand());
        commands.add(new BindCommand());
        commands.add(new FriendCommand());
        commands.add(new ConfigCommand());
    }

    public boolean processCommand(String message) {
        if (message == null || !message.startsWith(prefix)) {
            return false;
        }

        String raw = message.substring(prefix.length()).trim();
        if (raw.isEmpty()) {
            return false;
        }

        String[] split = raw.split("\\s+");
        String commandName = split[0];

        String[] args = new String[split.length - 1];
        System.arraycopy(split, 1, args, 0, args.length);

        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                try {
                    command.execute(args);
                } catch (Exception e) {
                    sendClientMessage("§cError executing command! Check console.");
                    e.printStackTrace();
                }
                return true;
            }
        }

        sendClientMessage("§cUnknown command! Type " + prefix + "toggle <module>.");
        return true;
    }

    public void sendClientMessage(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            mc.player.sendMessage(new TextComponentString("§7[§bSoleHack§7] " + message));
        }
    }
}