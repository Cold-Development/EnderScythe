package org.padrewin.enderscythe;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
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
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messageManager = new MessageManager(configManager);
        scytheManager = new ScytheManager(this, configManager);

        Bukkit.getPluginManager().registerEvents(new PlayerInteractionHandler(this, scytheManager, configManager, messageManager), this);
        this.getCommand("enderscythe").setExecutor(new CommandHandler(this, scytheManager, configManager, messageManager));
        this.getCommand("enderscythe").setTabCompleter(new CommandHandler(this, scytheManager, configManager, messageManager));
        this.getCommand("getupgradeitem").setExecutor(new UpgradeItemHandler(this, messageManager, scytheManager));
        this.getCommand("giveupgradeitem").setExecutor(new UpgradeItemHandler(this, messageManager, scytheManager));

        UpgradeItemHandler upgradeItemHandler = new UpgradeItemHandler(this, messageManager, scytheManager);
        upgradeItemHandler.register();

        startParticleTask();
        getServer().getPluginManager().registerEvents(new EventHandlerSmithing(this), this);
        getServer().getPluginManager().registerEvents(new EntityKillListener(scytheManager, this),this); // Înregistrează noul listener

        String name = getDescription().getName();
        getLogger().info("");
        getLogger().info("  ____ ___  _     ____  ");
        getLogger().info(" / ___/ _ \\| |   |  _ \\ ");
        getLogger().info("| |  | | | | |   | | | |");
        getLogger().info("| |__| |_| | |___| |_| |");
        getLogger().info(" \\____\\___/|_____|____/");
        getLogger().info("    " + name + " v" + getDescription().getVersion());
        getLogger().info("    Author(s): " + (String)getDescription().getAuthors().get(0));
        getLogger().info("    (c) Cold Development. All rights reserved.");
        getLogger().info("");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnderScythe disabled.");
        getLogger().info("See you soon :)");
    }

    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                scytheManager.spawnParticles();
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    public Set<UUID> getPlayersWithHoe() {
        return playersWithHoe;
    }

    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }

    public ScytheManager getScytheManager() {
        return this.scytheManager;
    }
}
