package org.sinNightMarket;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.sinNightMarket.commands.NightMarketCommand;
import org.sinNightMarket.listeners.MarketGUIListener;
import org.sinNightMarket.market.MarketManager;
import org.sinNightMarket.utils.Utils;

import java.util.Objects;

@SuppressWarnings("CallToPrintStackTrace")
public final class SinNightMarket extends JavaPlugin {

    private static SinNightMarket instance;
    private MarketManager marketManager;

    public static SinNightMarket getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Load messages
        try {
            Utils.saveDefaultMessages();
        } catch (Exception e) {
            getLogger().severe("Failed to load messages.yml!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup economy
        if (!setupEconomy()) {
            getLogger().warning("Vault not found! Economy features disabled.");
        }

        // Initialize MarketManager safely
        if (marketManager == null) {
            marketManager = new MarketManager();
        }
        try {
            marketManager.loadItems();
        } catch (Exception e) {
            getLogger().severe("Failed to load market items!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Cancel previous tasks to avoid duplicates
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().registerEvents(new MarketGUIListener(), this);
        // Schedule market checker
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                marketManager.checkMarketTime();
            } catch (Exception e) {
                getLogger().severe("Error in market time checker!");
                e.printStackTrace();
            }
        }, 0L, 1200L);

        // Register command safely
        if (getCommand("nightmarket") != null) {
            Objects.requireNonNull(getCommand("nightmarket")).setExecutor(new NightMarketCommand());
        } else {
            getLogger().severe("Command 'nightmarket' not found in plugin.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("NightMarket enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (marketManager != null) {
            try {
                marketManager.saveItems();
            } catch (Exception e) {
                getLogger().severe("Failed to save market items!");
                e.printStackTrace();
            }
        }

        // Cancel any scheduled tasks
        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("NightMarket disabled.");
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    /**
     * Setup economy if Vault exists
     * @return true if Vault is present, false otherwise
     */
    private boolean setupEconomy() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }
}
