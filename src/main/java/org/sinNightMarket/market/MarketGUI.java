package org.sinNightMarket.market;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sinNightMarket.SinNightMarket;
import org.sinNightMarket.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MarketGUI {

    private final Player player;
    private final MarketManager manager = SinNightMarket.getInstance().getMarketManager();
    private final int itemsPerPage;
    private Inventory gui;
    private final Map<Integer, ItemStorage> slotMap = new ConcurrentHashMap<>();

    private static final Map<Player, MarketGUI> openGUIs = new ConcurrentHashMap<>();
    public static final String GUI_TITLE = Utils.color("&6NightMarket");

    public MarketGUI(Player player) {
        this.player = player;
        this.itemsPerPage = SinNightMarket.getInstance().getConfig().getInt("market.items-per-page", 9);
    }

    public void open() {
        List<ItemStorage> items = manager.getMarketItems();
        int size = ((items.size() / 9) + 1) * 9;
        gui = Bukkit.createInventory(null, size, GUI_TITLE);
        slotMap.clear();

        for (int i = 0; i < items.size(); i++) {
            ItemStorage itemStorage = items.get(i);
            ItemStack display = Utils.addMarketLore(itemStorage.getItem().clone(), itemStorage, player);
            gui.setItem(i, display);
            slotMap.put(i, itemStorage);
        }

        player.openInventory(gui);
        openGUIs.put(player, this);
    }

    public boolean purchaseItem(Player player, ItemStorage item, int slot) {
        UUID uuid = player.getUniqueId();
        Map<UUID, Map<String, Integer>> purchases = manager.getPlayerPurchases();
        purchases.putIfAbsent(uuid, new ConcurrentHashMap<>());
        Map<String, Integer> userPurchases = purchases.get(uuid);

        String itemKey = item.getItem().getType().toString();
        int bought = userPurchases.getOrDefault(itemKey, 0);
        int limit = SinNightMarket.getInstance().getConfig().getInt("market.per-player-limit", 1);

        if (bought >= limit) {
            player.sendMessage(Utils.parseMessage(player, "purchase-limit", "%item%", itemKey));
            return false;
        }

        if (item.getStock() <= 0) {
            player.sendMessage(Utils.parseMessage(player, "out-of-stock", "%item%", itemKey));
            return false;
        }

        Economy econ = Utils.getEconomy();
        if (econ != null && !econ.has(player, item.getPrice())) {
            player.sendMessage(Utils.parseMessage(player, "insufficient-funds", "%price%", String.valueOf(item.getPrice())));
            return false;
        }

        if (econ != null) econ.withdrawPlayer(player, item.getPrice());

        item.decreaseStock();
        userPurchases.put(itemKey, bought + 1);
        player.getInventory().addItem(item.getItem().clone());
        player.sendMessage(Utils.parseMessage(player, "purchase-success", "%item%", itemKey, "%price%", String.valueOf(item.getPrice())));
        manager.saveItems();

        // Update only the purchased slot
        ItemStack updated = Utils.addMarketLore(item.getItem().clone(), item, player);
        gui.setItem(slot, updated);

        return true;
    }

    public Map<Integer, ItemStorage> getSlotMap() {
        return slotMap;
    }

    public static MarketGUI getOpenGUI(Player player) {
        return openGUIs.get(player);
    }

    public static void closeGUI(Player player) {
        openGUIs.remove(player);
    }

    public Inventory getInventory() {
        return gui;
    }
}
