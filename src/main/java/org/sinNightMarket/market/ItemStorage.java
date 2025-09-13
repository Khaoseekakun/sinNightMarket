package org.sinNightMarket.market;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sinNightMarket.utils.Utils;

import java.util.List;

public class ItemStorage {

    private final ItemStack item;
    private final double chance;
    private final int stockMin;
    private final int stockMax;
    private int stock;
    private final double price;

    public ItemStorage(ItemStack item, double chance, int stockMin, int stockMax, double price) {
        this.item = item;
        this.chance = chance;
        this.stockMin = stockMin;
        this.stockMax = stockMax;
        this.stock = Utils.randomInt(stockMin, stockMax);
        this.price = price;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getChance() {
        return chance;
    }

    public int getStock() {
        return stock;
    }

    public void decreaseStock() {
        stock = Math.max(0, stock - 1);
    }

    public double getPrice() {
        return price;
    }

    public void serialize(YamlConfiguration config, String path) {
        config.set(path + ".item", Utils.serializeItem(item));
        config.set(path + ".chance", chance);
        config.set(path + ".stockMin", stockMin);
        config.set(path + ".stockMax", stockMax);
        config.set(path + ".stock", stock);
        config.set(path + ".price", price);
    }

    public static ItemStorage deserialize(YamlConfiguration config, String path) {
        ItemStack item = Utils.deserializeItem(config.getString(path + ".item"));
        double chance = config.getDouble(path + ".chance");
        int stockMin = config.getInt(path + ".stockMin");
        int stockMax = config.getInt(path + ".stockMax");
        int stock = config.getInt(path + ".stock");
        double price = config.getDouble(path + ".price");

        ItemStorage storage = new ItemStorage(item, chance, stockMin, stockMax, price);
        storage.stock = stock;
        return storage;
    }
}
