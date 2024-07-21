package org.padrewin.enderscythe;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final EnderScythe plugin;
    private final ScytheManager scytheManager;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public CommandHandler(EnderScythe plugin, ScytheManager scytheManager, ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.scytheManager = scytheManager;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("enderscythe")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                configManager.reloadConfig();
                messageManager.reloadMessagesConfig();
                scytheManager.reloadConfigValues();
                sender.sendMessage(messageManager.getPrefixedMessage("config-reloaded"));
                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    scytheManager.giveEnderScythe(target, 1);
                    sender.sendMessage(messageManager.getPrefixedMessage("give-success").replace("{player}", target.getName()));
                    return true;
                } else {
                    sender.sendMessage(messageManager.getPrefixedMessage("player-not-found"));
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("upgrade")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    scytheManager.upgradeScythe(player);
                }
            } else {
                sender.sendMessage(messageManager.getPrefixedMessage("invalid-command"));
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("enderscythe")) {
            if (args.length == 1) {
                return Arrays.asList("give", "reload", "upgrade");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                return null;
            }
        }
        return Collections.emptyList();
    }
}
