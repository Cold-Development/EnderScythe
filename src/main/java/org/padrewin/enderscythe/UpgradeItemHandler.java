package org.padrewin.enderscythe;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpgradeItemHandler implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey upgradeKey;
    private final ScytheManager scytheManager;
    private final MessageManager messageManager;

    public UpgradeItemHandler(JavaPlugin plugin, MessageManager messageManager, ScytheManager scytheManager) {
        this.plugin = plugin;
        this.upgradeKey = new NamespacedKey(plugin, "scytheUpgradeItem");
        this.scytheManager = scytheManager;
        this.messageManager = messageManager;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("getupgradeitem").setExecutor(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            if (cursorItem != null && cursorItem.getType() == Material.AMETHYST_SHARD && isUpgradeItem(cursorItem)) {
                if (currentItem == null || currentItem.getType() == Material.AIR) {
                    return;
                }

                if (isEnderScythe(currentItem)) {
                    ItemMeta currentMeta = currentItem.getItemMeta();
                    if ((currentMeta != null && currentItem.getType() == Material.DIAMOND_HOE) || (currentMeta != null && currentItem.getType() == Material.NETHERITE_HOE)) {
                        int currentLevel = currentMeta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);
                        int maxLevel = scytheManager.getMaxScytheLevel();

                        if (currentLevel >= maxLevel) {
                            player.sendMessage(messageManager.getPrefixedMessage("max-level-reached"));
                            event.setCancelled(true);
                            return;
                        }

                        int newLevel = currentLevel + 1;
                        currentMeta = scytheManager.updateScytheLore(currentMeta, newLevel);
                        currentItem.setItemMeta(currentMeta);
                        event.setCancelled(true);
                        cursorItem.setAmount(cursorItem.getAmount() - 1);
                        player.sendMessage(messageManager.getPrefixedMessage("upgrade-success").replace("{level}", String.valueOf(newLevel))); // Feedback de succes
                    }
                } else {
                    player.sendMessage(messageManager.getPrefixedMessage("not-ender-scythe"));
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isUpgradeItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(upgradeKey, PersistentDataType.STRING);
    }

    private boolean isEnderScythe(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "isEnderScythe"), PersistentDataType.STRING);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("getupgradeitem")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack upgradeItem = createUpgradeItem();
                player.getInventory().addItem(upgradeItem);
                player.sendMessage(messageManager.getPrefixedMessage("receive-upgrade-item"));
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("giveupgradeitem")) {
            if (sender instanceof Player) {
                if (args.length >= 1) {  // Check if args has more than 1 element
                    String playerName = args[0];
                    Player player = plugin.getServer().getPlayer(playerName);
                    if (player != null) {  // Check if the player exists
                        ItemStack upgradeItem = createUpgradeItem();
                        player.getInventory().addItem(upgradeItem);
                        player.sendMessage(messageManager.getPrefixedMessage("receive-upgrade-item"));
                    } else {
                        sender.sendMessage(messageManager.getPrefixedMessage("player-not-found"));
                    }
                    return true;
                } else {
                    sender.sendMessage(messageManager.getPrefixedMessage("invalid-arguments"));
                    return false;
                }
            }
        }
        return false;
    }

    private ItemStack createUpgradeItem() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = plugin.getConfig().getString("upgrade-item.name");
            List<String> lore = plugin.getConfig().getStringList("upgrade-item.lore");

            if (displayName != null) {
                meta.setDisplayName(applyHexColors(displayName));
            }

            if (lore != null) {
                lore.replaceAll(this::applyHexColors);
                meta.setLore(lore);
            }

            meta.getPersistentDataContainer().set(upgradeKey, PersistentDataType.STRING, "upgrade");

            Random random = new Random();
            Enchantment[] enchantments = Enchantment.values();
            Enchantment randomEnchantment = enchantments[random.nextInt(enchantments.length)];
            meta.addEnchant(randomEnchantment, 1, true);

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }
        return item;
    }

    private String applyHexColors(String message) {
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length());

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = ChatColor.of("#" + hexColor).toString();
            matcher.appendReplacement(buffer, replacement);
        }

        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
