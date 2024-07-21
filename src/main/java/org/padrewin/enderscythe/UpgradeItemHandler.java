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

    public UpgradeItemHandler(JavaPlugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.upgradeKey = new NamespacedKey(plugin, "scytheUpgradeItem");
        this.scytheManager = new ScytheManager(plugin, new ConfigManager(plugin));
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

            // Verificăm dacă cursorItem este Scythe Shard
            if (cursorItem != null && cursorItem.getType() == Material.AMETHYST_SHARD && isUpgradeItem(cursorItem)) {
                // Dacă currentItem este null, înseamnă că încercăm să punem shardul într-un slot gol
                if (currentItem == null || currentItem.getType() == Material.AIR) {
                    return; // Permitem acțiunea fără a afișa vreun mesaj
                }

                // Verificăm dacă currentItem este Ender Scythe
                if (isEnderScythe(currentItem)) {
                    ItemMeta currentMeta = currentItem.getItemMeta();
                    if (currentMeta != null && currentItem.getType() == Material.DIAMOND_HOE) {
                        int currentLevel = currentMeta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);
                        int maxLevel = 2; // Definește nivelul maxim

                        if (currentLevel >= maxLevel) {
                            player.sendMessage(messageManager.getPrefixedMessage("max-level-reached"));
                            event.setCancelled(true);
                            return;
                        }

                        int newLevel = currentLevel + 1;
                        currentMeta = scytheManager.updateScytheLore(currentMeta, newLevel); // Actualizează lore-ul și numele
                        currentItem.setItemMeta(currentMeta);
                        event.setCancelled(true);
                        cursorItem.setAmount(cursorItem.getAmount() - 1); // Consuma unul dintre itemele de upgrade
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

    public ItemStack createEnderScythe(int level) {
        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta meta = hoe.getItemMeta();
        if (meta != null) {
            ScytheManager scytheManager = new ScytheManager(plugin, new ConfigManager(plugin)); // Inițializarea ScytheManager
            String baseName = scytheManager.generateScytheName();
            String levelTemplate = scytheManager.generateScytheLevel(level);
            String displayName = baseName + " " + levelTemplate;
            meta.setDisplayName(displayName);

            List<String> updatedLore = scytheManager.generateScytheLore(level);
            meta.setLore(updatedLore);

            NamespacedKey enderScytheKey = new NamespacedKey(plugin, "isEnderScythe"); // Definirea enderScytheKey
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, level);
            meta.getPersistentDataContainer().set(enderScytheKey, PersistentDataType.STRING, "true");
            hoe.setItemMeta(meta);
        }
        return hoe;
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
