package org.padrewin.enderscythe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractionHandler implements Listener {

    private final EnderScythe plugin;
    private final ScytheManager scytheManager;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public PlayerInteractionHandler(EnderScythe plugin, ScytheManager scytheManager, ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.scytheManager = scytheManager;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if ((item != null && item.getType() == Material.DIAMOND_HOE) || (item != null && item.getType() == Material.NETHERITE_HOE)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && scytheManager.isEnderScythe(item)) {
                // Verifică dacă jucătorul se află într-un world permis
                if (!configManager.getConfig().getStringList("enderscythe-use-worlds").contains(player.getWorld().getName())) {
                    player.sendMessage(messageManager.getPrefixedMessage("not-allowed-world"));
                    return;
                }

                // Anulează evenimentul pentru a preveni transformarea blocurilor în farmland
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    event.setCancelled(true);
                    // Logica de utilizare a scythe-ului
                    scytheManager.useScythe(player, item);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if ((item != null && item.getType() == Material.DIAMOND_HOE && scytheManager.isEnderScythe(item)) || (item != null && item.getType() == Material.NETHERITE_HOE && scytheManager.isEnderScythe(item))) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int level = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);
                String baseName = scytheManager.generateScytheName();
                String levelTemplate = scytheManager.generateScytheLevel(level);
                String configName = baseName + " " + levelTemplate;
                if (meta.getDisplayName().equals(configName)) {
                    plugin.getPlayersWithHoe().add(player.getUniqueId());
                    return;
                }
            }
        }
        plugin.getPlayersWithHoe().remove(player.getUniqueId());
    }
}