package org.padrewin.enderscythe;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class EventHandlerSmithing implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey enderScytheKey;
    private final MessageManager messageManager;

    public EventHandlerSmithing(JavaPlugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.enderScytheKey = new NamespacedKey(plugin, "isEnderScythe");
        this.messageManager = messageManager;
    }

    private boolean isEnderScythe(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(enderScytheKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack baseItem = event.getInventory().getItem(0);
        ItemStack upgradeItem = event.getInventory().getItem(1);

        if (baseItem != null && upgradeItem != null && isEnderScythe(baseItem)) {
            event.setResult(null); // Anulăm rezultatul pentru a preveni conversia în hoe de netherite
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (inventory.getType().toString().equals("SMITHING") || inventory.getType().toString().equals("ANVIL") || inventory.getType().toString().equals("GRINDSTONE")) {
            if (currentItem != null && isEnderScythe(currentItem)) {
                event.setCancelled(true); // Anulăm evenimentul pentru a preveni utilizarea în Smithing Table
                event.getWhoClicked().sendMessage(messageManager.getPrefixedMessage("denied-inventory"));
            }

            if (cursorItem != null && isEnderScythe(cursorItem)) {
                event.setCancelled(true); // Anulăm evenimentul pentru a preveni utilizarea în Smithing Table
                event.getWhoClicked().sendMessage(messageManager.getPrefixedMessage("denied-inventory"));
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        if ((firstItem != null && isEnderScythe(firstItem)) || (secondItem != null && isEnderScythe(secondItem))) {
            event.setResult(null); // Anulăm rezultatul pentru a preveni combinarea itemelor
        }
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);

        if ((firstItem != null && isEnderScythe(firstItem)) || (secondItem != null && isEnderScythe(secondItem))) {
            event.setResult(null); // Anulăm rezultatul pentru a preveni ștergerea enchantmenturilor
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        if (item != null && isEnderScythe(item)) {
            event.setCancelled(true); // Anulăm evenimentul de enchant
            event.getEnchanter().sendMessage(messageManager.getPrefixedMessage("denied-inventory"));
        }
    }
}
