package org.padrewin.enderscythe;

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
                    meta.setDisplayName(baseMeta.getDisplayName());
                    meta.setLore(baseMeta.getLore());

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
}