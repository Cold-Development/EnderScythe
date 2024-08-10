package org.padrewin.enderscythe;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityKillListener implements Listener {

    private final ScytheManager scytheManager;
    private final EnderScythe plugin;

    public EntityKillListener(ScytheManager scytheManager, EnderScythe plugin) {
        this.scytheManager = scytheManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (scytheManager.isEnderScythe(player.getInventory().getItemInMainHand())) {
                LivingEntity entity = (LivingEntity) event.getEntity();
                double damage = event.getDamage();
                double health = entity.getHealth();

                // Verificăm dacă damage-ul este suficient pentru a omorî entitatea
                if (damage >= health) {
                    entity.setMetadata("enderScytheKiller", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                    entity.setLastDamageCause(event); // Setăm cauza ultimei daune
                }
            }
        }
    }
}
