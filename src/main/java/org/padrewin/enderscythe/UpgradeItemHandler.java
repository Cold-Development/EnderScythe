package org.padrewin.enderscythe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
        return false;
    }

    private ItemStack createUpgradeItem() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = plugin.getConfig().getString("upgrade-item.name");
            List<String> lore = plugin.getConfig().getStringList("upgrade-item.lore");

            if (displayName != null) {
                meta.setDisplayName(displayName);
            }

            if (lore != null) {
                meta.setLore(lore);
            }

            meta.getPersistentDataContainer().set(upgradeKey, PersistentDataType.STRING, "upgrade");
            item.setItemMeta(meta);
        }
        return item;
    }
}
