package org.padrewin.enderscythe;

import net.md_5.bungee.api.ChatColor;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                // Anulează evenimentul dacă jucătorul folosește EnderScythe pe un bloc
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                }

                if (!configManager.getConfig().getStringList("enderscythe-use-worlds").contains(player.getWorld().getName())) {
                    player.sendMessage(applyHexColors(messageManager.getPrefixedMessage("not-allowed-world")));
                    return;
                }

                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    event.setCancelled(true);

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
