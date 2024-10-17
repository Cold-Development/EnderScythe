package org.padrewin.enderscythe;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.manager.Manager;
import dev.padrewin.colddev.manager.PluginUpdateManager;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EnderScythe extends ColdPlugin implements Listener, CommandExecutor, TabExecutor {

    private static EnderScythe instance;
    private final Set<UUID> playersWithHoe = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ScytheManager scytheManager;

    public EnderScythe() {
        super("Cold-Development", "EnderScythe", 23657, null, null, null);
        instance = this;
    }

    public static EnderScythe getInstance() {
        return instance;
    }

    @Override
    public void enable() {
        instance = this;
        saveDefaultConfig();
        getManager(PluginUpdateManager.class);

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
        getServer().getPluginManager().registerEvents(new EntityKillListener(scytheManager, this),this);

        String name = getDescription().getName();
        getLogger().info("");
        getLogger().info("  ____ ___  _     ____  ");
        getLogger().info(" / ___/ _ \\| |   |  _ \\ ");
        getLogger().info("| |  | | | | |   | | | |");
        getLogger().info("| |__| |_| | |___| |_| |");
        getLogger().info(" \\____\\___/|_____|____/");
        getLogger().info("    " + name + " v" + getDescription().getVersion());
        getLogger().info("    Author(s): " + (String)getDescription().getAuthors().get(0));
        getLogger().info("    (c) Cold Development ❄");
        getLogger().info("");
    }

    @Override
    public void disable() {
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

    @Override
    protected @NotNull List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of();
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
