package org.padrewin.enderscythe;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;

public class EnderScythe extends JavaPlugin implements Listener, CommandExecutor, TabExecutor {

    private final Set<UUID> playersWithHoe = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ScytheManager scytheManager;

    @Override
    public void onEnable() {
        // Salvează fișierul de configurare implicit dacă nu există
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messageManager = new MessageManager(configManager);
        scytheManager = new ScytheManager(this, configManager);

        Bukkit.getPluginManager().registerEvents(new PlayerInteractionHandler(this, scytheManager, configManager, messageManager), this);
        this.getCommand("enderscythe").setExecutor(new CommandHandler(this, scytheManager, configManager, messageManager));
        this.getCommand("enderscythe").setTabCompleter(new CommandHandler(this, scytheManager, configManager, messageManager));
        this.getCommand("getupgradeitem").setExecutor(new UpgradeItemHandler(this, messageManager));

        UpgradeItemHandler upgradeItemHandler = new UpgradeItemHandler(this, messageManager);
        upgradeItemHandler.register();

        scytheManager.startParticleTask();  // Mutați apelul metodei aici
        Bukkit.getPluginManager().registerEvents(new EventHandlerSmithing(this, messageManager), this);

        getLogger().info("EnderScythe has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnderScythe has been disabled.");
    }

    public Set<UUID> getPlayersWithHoe() {
        return playersWithHoe;
    }

    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}