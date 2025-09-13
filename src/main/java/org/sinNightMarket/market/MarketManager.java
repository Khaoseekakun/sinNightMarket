package org.sinNightMarket.market;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sinNightMarket.SinNightMarket;
import org.sinNightMarket.utils.Utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarketManager {

    private final Map<UUID, Map<String, Long>> lastPurchaseTime = new ConcurrentHashMap<>();
    private final List<ItemStorage> marketItems = Collections.synchronizedList(new ArrayList<>());
    private final Map<UUID, Map<String, Integer>> playerPurchases = new ConcurrentHashMap<>();
    private final SinNightMarket plugin = SinNightMarket.getInstance();

    private LocalTime startTime;
    private LocalTime endTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public MarketManager() {
        loadConfigTimes();
    }

    public void loadConfigTimes() {
        startTime = LocalTime.parse(plugin.getConfig().getString("market.start"), formatter);
        endTime = LocalTime.parse(plugin.getConfig().getString("market.end"), formatter);
    }

    public boolean isMarketOpen() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    public void checkMarketTime() {
        boolean open = isMarketOpen();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (open) {
                // Optionally notify open
            }
        });
    }

    public void addItem(ItemStorage item) {
        marketItems.add(item);
        saveItems();
    }

    public List<ItemStorage> getMarketItems() {
        return marketItems;
    }

    public Map<UUID, Map<String, Integer>> getPlayerPurchases() {
        return playerPurchases;
    }

    public void saveItems() {
        Utils.saveItems(marketItems);
    }

    public void loadItems() {
        marketItems.clear();
        marketItems.addAll(Utils.loadItems());
    }

    public void reloadItems() {
        loadConfigTimes();
        loadItems();
    }

    public String getStartTime() {
        return startTime.format(formatter);
    }

    public String getEndTime() {
        return endTime.format(formatter);
    }

    public LocalTime getStartTimeLocal() {
        return startTime;
    }

    public LocalTime getEndTimeLocal() {
        return endTime;
    }

    public void recordPurchase(Player player, String itemKey) {
        lastPurchaseTime.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
        lastPurchaseTime.get(player.getUniqueId()).put(itemKey, System.currentTimeMillis());
    }

    public long getCooldown(Player player, String itemKey) {
        return lastPurchaseTime.getOrDefault(player.getUniqueId(), Map.of())
                .getOrDefault(itemKey, 0L);
    }
}
