package org.padrewin.enderscythe;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class ScytheManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Set<UUID> playersWithScythe = new HashSet<>();
    private final NamespacedKey enderScytheKey;

    // Variabile pentru valorile de configurare
    private double enderScytheDamage;
    private long enderScytheCooldown;
    private int enderScytheRange;
    private boolean damagePlayers;
    private String laserColor;
    private boolean level1ParticlesEnabled;
    private boolean level2ParticlesEnabled;

    public ScytheManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.enderScytheKey = new NamespacedKey(plugin, "isEnderScythe");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Inițializare valori de configurare
        reloadConfigValues();

        // Task repetitiv pentru a adăuga particule
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::spawnParticles, 0L, 10L); // rulează la fiecare 10 ticks
    }

    public void reloadConfigValues() {
        this.enderScytheDamage = configManager.getConfig().getDouble("enderscythe-damage", 5.0);
        this.enderScytheCooldown = configManager.getConfig().getLong("enderscythe-cooldown", 500);
        this.enderScytheRange = configManager.getConfig().getInt("enderscythe-range", 30);
        this.damagePlayers = configManager.getConfig().getBoolean("damage-players", true);
        this.laserColor = configManager.getConfig().getString("ender-scythe.laser-color", "#800080"); // Implicit PURPLE
        this.level1ParticlesEnabled = configManager.getConfig().getBoolean("ender-scythe-level1-particles", true);
        this.level2ParticlesEnabled = configManager.getConfig().getBoolean("ender-scythe-level2-particles", true);
    }

    public void startParticleTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::spawnParticles, 0L, 10L); // Rulează la fiecare 10 ticks
    }

    private void spawnParticles() {
        for (UUID playerId : playersWithScythe) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && isEnderScythe(item)) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        int level = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);

                        if (level == 1) {
                            if (level1ParticlesEnabled) {
                                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 30, 0.5, 1, 0.5, 0);
                            }
                        } else if (level == 2) {
                            if (level2ParticlesEnabled) {
                                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);
                                player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(), 30, 0.5, 1, 0.5, 0, dustOptions);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isEnderScythe(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(enderScytheKey, PersistentDataType.STRING);
    }

    public double getEnderScytheDamage() {
        return enderScytheDamage;
    }

    public long getEnderScytheCooldown() {
        return enderScytheCooldown;
    }

    public int getEnderScytheRange() {
        return enderScytheRange;
    }

    public boolean isDamagePlayers() {
        return configManager.getConfig().getBoolean("damage-players");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public boolean isPvPAllowed(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(player.getLocation()), WorldGuardPlugin.inst().wrapPlayer(player), Flags.PVP);
    }

    public String generateScytheName() {
        return applyHexColors(Objects.requireNonNull(configManager.getConfig().getString("ender-scythe.name")));
    }

    public String generateScytheLevel(int level) {
        String levelTemplate = configManager.getConfig().getString("ender-scythe.level");
        if (levelTemplate == null) {
            //plugin.getLogger().severe("Config key 'ender-scythe.level' not found in config.yml");
            return "§8「" + level + "§8」";
        }
        return applyHexColors(levelTemplate.replace("%scythe_level%", String.valueOf(level)));
    }

    public List<String> generateScytheLore(int level) {
        List<String> loreLines = configManager.getConfig().getStringList("ender-scythe.lore");
        List<String> updatedLore = new ArrayList<>();
        for (String line : loreLines) {
            updatedLore.add(applyPlaceholders(line, level));
        }
        return updatedLore;
    }

    public ItemStack createEnderScythe(int level) {
        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta meta = hoe.getItemMeta();
        if (meta != null) {
            String baseName = generateScytheName();
            String levelTemplate = generateScytheLevel(level);
            String displayName = baseName + " " + levelTemplate;
            meta.setDisplayName(displayName);

            List<String> updatedLore = generateScytheLore(level);
            meta.setLore(updatedLore);

            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, level);
            meta.getPersistentDataContainer().set(enderScytheKey, PersistentDataType.STRING, "true");
            hoe.setItemMeta(meta);
        }
        return hoe;
    }

    public String applyPlaceholders(String text, int level) {
        return text.replace("%enderscythe_damage%", String.valueOf(getEnderScytheDamage() * Math.pow(2, level - 1)))
                .replace("%enderscythe_range%", String.valueOf(getEnderScytheRange()))
                .replace("%enderscythe_cooldown%", String.valueOf(getEnderScytheCooldown() / 1000.0));
    }


    public ItemMeta updateScytheLevelInName(ItemMeta meta, int newLevel) {
        if (meta != null) {
            String currentName = meta.getDisplayName();
            String baseName = currentName.split("「")[0].trim();
            String levelTemplate = generateScytheLevel(newLevel);
            String newName = baseName + levelTemplate;
            meta.setDisplayName(newName);
        }
        return meta;
    }

    public ItemMeta updateScytheLore(ItemMeta meta, int newLevel) {
        if (meta != null) {
            // Actualizează persistent data container pentru noul nivel
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, newLevel);
            meta.getPersistentDataContainer().set(enderScytheKey, PersistentDataType.STRING, "true");

            // Actualizează doar nivelul în nume și valorile din lore
            meta = updateScytheLevelInName(meta, newLevel);

            List<String> updatedLore = generateScytheLore(newLevel);
            meta.setLore(updatedLore);
        }
        return meta;
    }

    public void giveEnderScythe(Player player, int level) {
        ItemStack enderScythe = createEnderScythe(level);
        player.getInventory().addItem(enderScythe);
        String message = configManager.getMessagesConfig().getString("messages.receive-hoe");
        if (message != null) {
            player.sendMessage(message);
        }
    }

    public void upgradeScythe(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.DIAMOND_HOE && isEnderScythe(item)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int currentLevel = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);
                int maxLevel = 2; // Definește nivelul maxim

                if (currentLevel >= maxLevel) {
                    player.sendMessage(configManager.getMessagesConfig().getString("messages.max-level-reached"));
                    return;
                }

                int newLevel = currentLevel + 1;
                meta = updateScytheLore(meta, newLevel);  // Actualizăm doar lore-ul și numele cu nivelul corect
                item.setItemMeta(meta);
                player.sendMessage(configManager.getMessagesConfig().getString("upgrade-success").replace("{level}", String.valueOf(newLevel)));
            }
        }
    }

    public void useScythe(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(enderScytheKey, PersistentDataType.STRING)) {
            UUID playerId = player.getUniqueId();
            Map<UUID, Long> cooldowns = ((EnderScythe) plugin).getCooldowns();

            if (cooldowns.containsKey(playerId)) {
                long timeLeft = (cooldowns.get(playerId) + getEnderScytheCooldown()) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    return;
                }
            }

            cooldowns.put(playerId, System.currentTimeMillis());

            int scytheLevel = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);
            double damage = getEnderScytheDamage() * Math.pow(2, scytheLevel - 1);

            Vector direction = player.getLocation().getDirection();
            Vector offset = direction.clone().normalize().multiply(0.5);
            offset.setY(-0.5);

            Color laserColor = getLaserColor();

            Particle.DustOptions dustOptions = new Particle.DustOptions(laserColor, 1.0F);
            for (int i = 0; i < getEnderScytheRange(); i++) {
                Vector point = direction.clone().multiply(i).add(player.getEyeLocation().toVector()).add(offset);
                Material blockType = player.getWorld().getBlockAt(point.toLocation(player.getWorld())).getType();
                if (blockType != Material.AIR && blockType != Material.WATER && blockType != Material.LAVA) {
                    break;
                }
                player.getWorld().spawnParticle(Particle.REDSTONE, point.toLocation(player.getWorld()), 10, 0, 0, 0, 0, dustOptions);

                // Verifică dacă laserul lovește o entitate
                for (Entity entity : player.getWorld().getNearbyEntities(point.toLocation(player.getWorld()), 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity) {
                        if (entity != player) {
                            if (entity instanceof Player) {
                                if (isDamagePlayers() && isPvPAllowed(player)) {
                                    ((LivingEntity) entity).damage(damage);
                                }
                            } else {
                                ((LivingEntity) entity).damage(damage);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && item.getType() == Material.DIAMOND_HOE && isEnderScythe(item)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int level = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);
                String baseName = generateScytheName();
                String levelTemplate = generateScytheLevel(level);
                String configName = baseName + " " + levelTemplate;
                if (meta.getDisplayName().equals(configName)) {
                    playersWithScythe.add(player.getUniqueId());
                    return;
                }
            }
        }
        playersWithScythe.remove(player.getUniqueId());
    }

    private Color getLaserColor() {
        return Color.fromRGB(
                Integer.valueOf(laserColor.substring(1, 3), 16),
                Integer.valueOf(laserColor.substring(3, 5), 16),
                Integer.valueOf(laserColor.substring(5, 7), 16)
        );
    }

    private String applyHexColors(String message) {
        StringBuilder sb = new StringBuilder();
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 7 < chars.length && chars[i + 1] == '#' &&
                    isHexChar(chars[i + 2]) && isHexChar(chars[i + 3]) && isHexChar(chars[i + 4]) &&
                    isHexChar(chars[i + 5]) && isHexChar(chars[i + 6]) && isHexChar(chars[i + 7])) {
                sb.append("§x");
                for (int j = 2; j <= 7; j++) {
                    sb.append('§').append(chars[i + j]);
                }
                i += 7; // Skip the next 7 characters as they are part of the hex color code
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }
}
