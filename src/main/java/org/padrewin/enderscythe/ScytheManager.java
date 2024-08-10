package org.padrewin.enderscythe;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScytheManager implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Set<UUID> playersWithScythe = new HashSet<>();
    private final NamespacedKey enderScytheKey;

    private double enderScytheDamage;
    private long enderScytheCooldown;
    private int enderScytheRange;
    private boolean damagePlayers;
    private String laserColor;
    private boolean particlesEnabled;
    private int maxScytheLevel;
    private Map<Integer, ParticleSettings> particleSettingsMap;

    public ScytheManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.enderScytheKey = new NamespacedKey(plugin, "isEnderScythe");
        this.particleSettingsMap = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        reloadConfigValues();

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::spawnParticles, 0L, 10L);
    }


    public void reloadConfigValues() {
        this.enderScytheDamage = configManager.getConfig().getDouble("enderscythe-damage", 5.0);
        this.enderScytheCooldown = configManager.getConfig().getLong("enderscythe-cooldown", 500);
        this.enderScytheRange = configManager.getConfig().getInt("enderscythe-range", 30);
        this.damagePlayers = configManager.getConfig().getBoolean("damage-players", true);
        this.laserColor = configManager.getConfig().getString("ender-scythe.laser-color", "#800080");
        this.maxScytheLevel = Math.max(configManager.getConfig().getInt("enderscythe-max-level", 2), 1);
        this.particlesEnabled = configManager.getConfig().getBoolean("enderscythe-particles", true);

        // Clear and reload particle settings
        this.particleSettingsMap.clear();
        ConfigurationSection particleSection = configManager.getConfig().getConfigurationSection("particle-settings");
        if (particleSection != null) {
            for (String key : particleSection.getKeys(false)) {
                int level = Integer.parseInt(key);
                ConfigurationSection section = particleSection.getConfigurationSection(key);
                if (section != null) {
                    this.particleSettingsMap.put(level, new ParticleSettings(section));
                }
            }
        }
    }

    private void loadParticleSettings() {
        ConfigurationSection section = configManager.getConfig().getConfigurationSection("particle-settings");
        if (section != null) {
            particleSettingsMap.clear();
            for (String key : section.getKeys(false)) {
                int level = Integer.parseInt(key);
                ParticleSettings settings = new ParticleSettings(section.getConfigurationSection(key));
                particleSettingsMap.put(level, settings);
            }
        }
    }

    public int getMaxScytheLevel() {
        return maxScytheLevel;
    }

    void spawnParticles() {
        if (!particlesEnabled) return;

        for (UUID playerId : playersWithScythe) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && isEnderScythe(item)) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        int level = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);

                        if (particleSettingsMap.containsKey(level)) {
                            ParticleSettings settings = particleSettingsMap.get(level);
                            if (settings.getType() == Particle.REDSTONE) {
                                Particle.DustOptions dustOptions = new Particle.DustOptions(settings.getColor(), settings.getSize());
                                player.getWorld().spawnParticle(
                                        settings.getType(),
                                        player.getLocation(),
                                        settings.getCount(),
                                        settings.getOffsetX(),
                                        settings.getOffsetY(),
                                        settings.getOffsetZ(),
                                        settings.getExtra(),
                                        dustOptions
                                );
                            } else {
                                player.getWorld().spawnParticle(
                                        settings.getType(),
                                        player.getLocation(),
                                        settings.getCount(),
                                        settings.getOffsetX(),
                                        settings.getOffsetY(),
                                        settings.getOffsetZ(),
                                        settings.getExtra()
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ParticleSettings {
        private final Particle type;
        private final int count;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        private final double extra;
        private final Color color;
        private final float size;

        public ParticleSettings(ConfigurationSection section) {
            this.type = Particle.valueOf(section.getString("type"));
            this.count = section.getInt("count", 30);
            this.offsetX = section.getDouble("offsetX", 0.5);
            this.offsetY = section.getDouble("offsetY", 1);
            this.offsetZ = section.getDouble("offsetZ", 0.5);
            this.extra = section.getDouble("extra", 0);
            if (type == Particle.REDSTONE) {
                String colorStr = section.getString("color", "#FF0000"); // Default red color
                this.color = Color.fromRGB(
                        Integer.valueOf(colorStr.substring(1, 3), 16),
                        Integer.valueOf(colorStr.substring(3, 5), 16),
                        Integer.valueOf(colorStr.substring(5, 7), 16)
                );
                this.size = (float) section.getDouble("size", 1.0);
            } else {
                this.color = null;
                this.size = 0;
            }
        }

        public Particle getType() {
            return type;
        }

        public int getCount() {
            return count;
        }

        public double getOffsetX() {
            return offsetX;
        }

        public double getOffsetY() {
            return offsetY;
        }

        public double getOffsetZ() {
            return offsetZ;
        }

        public double getExtra() {
            return extra;
        }

        public Color getColor() {
            return color;
        }

        public float getSize() {
            return size;
        }
    }

    public boolean isEnderScythe(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material itemType = item.getType();
        if (itemType != Material.DIAMOND_HOE && itemType != Material.NETHERITE_HOE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(enderScytheKey, PersistentDataType.STRING);
    }

    public JavaPlugin getPlugin() {
        return plugin;
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
        return damagePlayers;
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
        String name = configManager.getConfig().getString("ender-scythe.name");
        return name != null ? applyHexColors(name) : null;
    }

    public String generateScytheLevel(int level) {
        String levelTemplate = configManager.getConfig().getString("ender-scythe.level");
        if (levelTemplate == null) {
            return applyHexColors("&8「" + level + "&8」");
        }
        return applyHexColors(levelTemplate.replace("%scythe_level%", String.valueOf(level)));
    }

    public List<String> generateScytheLore(int level) {
        List<String> loreLines = configManager.getConfig().getStringList("ender-scythe.lore");
        List<String> updatedLore = new ArrayList<>();
        for (String line : loreLines) {
            updatedLore.add(applyHexColors(applyPlaceholders(line, level)));
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

            meta.addEnchant(Enchantment.DURABILITY, 1, true);

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

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
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, newLevel);
            meta.getPersistentDataContainer().set(enderScytheKey, PersistentDataType.STRING, "true");
            meta = updateScytheLevelInName(meta, newLevel);
            List<String> updatedLore = generateScytheLore(newLevel);
            meta.setLore(updatedLore);
        }
        return meta;
    }

    public void giveEnderScythe(Player player, int level) {
        ItemStack enderScythe = createEnderScythe(level);
        player.getInventory().addItem(enderScythe);
        String pluginPrefix = configManager.getConfig().getString("plugin-prefix");
        String message = configManager.getMessagesConfig().getString("messages.receive-hoe");

        if (message != null && pluginPrefix != null) {
            String prefixedMessage = pluginPrefix + "" + message;

            player.sendMessage(applyHexColors(prefixedMessage));
        }
    }

    public void upgradeScythe(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && (item.getType() == Material.DIAMOND_HOE || item.getType() == Material.NETHERITE_HOE) && isEnderScythe(item)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int currentLevel = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "scytheLevel"), PersistentDataType.INTEGER, 1);

                if (currentLevel >= maxScytheLevel) {
                    player.sendMessage(Objects.requireNonNull(configManager.getMessagesConfig().getString("messages.max-level-reached")));
                    return;
                }

                int newLevel = currentLevel + 1;
                meta = updateScytheLore(meta, newLevel);
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

                for (Entity entity : player.getWorld().getNearbyEntities(point.toLocation(player.getWorld()), 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity) {
                        if (entity != player) {
                            if (entity instanceof Player) {
                                if (isDamagePlayers() && isPvPAllowed(player)) {
                                    ((LivingEntity) entity).damage(damage, player); // damage caused by player
                                }
                            } else {
                                ((LivingEntity) entity).damage(damage, player); // damage caused by player
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
        if (item != null && isEnderScythe(item)) {
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
