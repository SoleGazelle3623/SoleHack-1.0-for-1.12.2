package com.yourwebsitespace.solehack.command.commands;

import com.yourwebsitespace.solehack.FriendManager;
import com.yourwebsitespace.solehack.command.Command;
import com.yourwebsitespace.solehack.command.CommandManager;

import java.util.List;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Manages your client friend list", "friend <add/remove/list> [name]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandManager.INSTANCE.sendClientMessage("§cUsage: " + getUsage());
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 2) {
                    CommandManager.INSTANCE.sendClientMessage("§cUsage: ;friend add <name>");
                    return;
                }
                String addName = args[1];
                if (FriendManager.INSTANCE.isFriend(addName)) {
                    CommandManager.INSTANCE.sendClientMessage("§e" + addName + " §7is already your friend!");
                    return;
                }
                FriendManager.INSTANCE.addFriend(addName);
                CommandManager.INSTANCE.sendClientMessage("§7Added §a" + addName + " §7to your friends list.");
                break;

            case "remove":
                if (args.length < 2) {
                    CommandManager.INSTANCE.sendClientMessage("§cUsage: ;friend remove <name>");
                    return;
                }
                String remName = args[1];
                if (!FriendManager.INSTANCE.isFriend(remName)) {
                    CommandManager.INSTANCE.sendClientMessage("§e" + remName + " §7is not on your friends list.");
                    return;
                }
                FriendManager.INSTANCE.removeFriend(remName);
                CommandManager.INSTANCE.sendClientMessage("§7Removed §c" + remName + " §7from your friends list.");
                break;

            case "list":
                List<String> friends = FriendManager.INSTANCE.getFriends();
                if (friends.isEmpty()) {
                    CommandManager.INSTANCE.sendClientMessage("§7Your friends list is empty.");
                    return;
                }
                CommandManager.INSTANCE.sendClientMessage("§7Friends (" + friends.size() + "): §e" + String.join(", ", friends));
                break;

            default:
                CommandManager.INSTANCE.sendClientMessage("§cUnknown action! Use add, remove, or list.");
                break;
        }
    }
}
