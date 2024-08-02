package org.padrewin.enderscythe;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventHandlerSmithing implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey enderScytheKey;

    public EventHandlerSmithing(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enderScytheKey = new NamespacedKey(plugin, "isEnderScythe");
    }

    private boolean isEnderScythe(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        return (item.getType() == Material.DIAMOND_HOE || item.getType() == Material.NETHERITE_HOE) &&
                meta != null && meta.getPersistentDataContainer().has(enderScytheKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack baseItem = event.getInventory().getItem(0);
        ItemStack upgradeItem = event.getInventory().getItem(1);

        if (baseItem != null && upgradeItem != null && isEnderScythe(baseItem) && upgradeItem.getType() == Material.NETHERITE_INGOT) {
            ItemStack resultItem = new ItemStack(Material.NETHERITE_HOE);
            ItemMeta meta = resultItem.getItemMeta();
            if (meta != null) {
                ItemMeta baseMeta = baseItem.getItemMeta();
                if (baseMeta != null) {
                    meta.setDisplayName(applyHexColors(baseMeta.getDisplayName()));
                    meta.setLore(applyHexColors(baseMeta.getLore()));

                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, baseMeta.getPersistentDataContainer().get(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER));
                    meta.getPersistentDataContainer().set(enderScytheKey, PersistentDataType.STRING, "true");
                    resultItem.setItemMeta(meta);
                    event.setResult(resultItem);
                }
            }
        }
    }

    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        if (isEnderScythe(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        if ((firstItem != null && isEnderScythe(firstItem)) || (secondItem != null && isEnderScythe(secondItem))) {
            event.setResult(new ItemStack(Material.AIR)); //
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        ItemStack[] items = event.getInventory().getMatrix();

        for (ItemStack item : items) {
            if (isEnderScythe(item)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                break;
            }
        }
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        if ((firstItem != null && isEnderScythe(firstItem)) || (secondItem != null && isEnderScythe(secondItem))) {
            event.setResult(new ItemStack(Material.AIR));
        }
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

    private List<String> applyHexColors(List<String> messages) {
        List<String> coloredMessages = new ArrayList<>();
        for (String message : messages) {
            coloredMessages.add(applyHexColors(message));
        }
        return coloredMessages;
    }
}
