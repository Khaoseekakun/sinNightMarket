package org.sinNightMarket.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sinNightMarket.SinNightMarket;
import org.sinNightMarket.market.ItemStorage;
import org.sinNightMarket.market.MarketManager;

import java.io.*;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class Utils {

    private static YamlConfiguration messages;
    private static final Random random = new Random();

    public static void saveDefaultMessages() {
        File file = new File(SinNightMarket.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) SinNightMarket.getInstance().saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static void reloadMessages() {
        File file = new File(SinNightMarket.getInstance().getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String parsePlaceholders(Player player, String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return color(message);
    }

    public static String parseMessage(Player player, String key, String... placeholders) {
        String msg = messages.getString(key, "&cMessage not found");
        if (placeholders != null) {
            for (int i = 0; i < placeholders.length - 1; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return parsePlaceholders(player, msg);
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static void saveItems(List<ItemStorage> items) {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < items.size(); i++) items.get(i).serialize(config, "items." + i);
        try {
            config.save(new File(SinNightMarket.getInstance().getDataFolder(), "market-items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ItemStorage> loadItems() {
        File file = new File(SinNightMarket.getInstance().getDataFolder(), "market-items.yml");
        if (!file.exists()) {
            // If file doesn't exist, create an empty one
            try {
                file.createNewFile();
                YamlConfiguration emptyConfig = new YamlConfiguration();
                emptyConfig.set("items", null);
                emptyConfig.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new java.util.ArrayList<>();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<ItemStorage> list = new java.util.ArrayList<>();

        if (config.contains("items") && config.getConfigurationSection("items") != null) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                ItemStorage item = ItemStorage.deserialize(config, "items." + key);
                if (item != null) list.add(item);
            }
        }

        return list;
    }


    public static String serializeItem(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (org.bukkit.util.io.BukkitObjectOutputStream dataOutput = new org.bukkit.util.io.BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack deserializeItem(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try (org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack addMarketLore(ItemStack item, ItemStorage storage, Player player) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();
            lore.add(parsePlaceholders(player, "&7Price: &e" + storage.getPrice()));
            lore.add(parsePlaceholders(player, "&7Stock: &a" + storage.getStock()));
            meta.setLore(lore);
            clone.setItemMeta(meta);
        }
        return clone;
    }

    public static Economy getEconomy() {
        return Bukkit.getServicesManager().getRegistration(Economy.class) != null ?
                Bukkit.getServicesManager().getRegistration(Economy.class).getProvider() : null;
    }

    public static String parseCooldown(Player player, String itemKey, String message) {
        long lastTime = SinNightMarket.getInstance().getMarketManager().getCooldown(player, itemKey);
        long cooldown = SinNightMarket.getInstance().getConfig().getLong("market.cooldown-ms", 3600000); // default 1 hour
        long remaining = Math.max(0, (lastTime + cooldown) - System.currentTimeMillis());

        long seconds = remaining / 1000 % 60;
        long minutes = remaining / (1000 * 60) % 60;
        long hours = remaining / (1000 * 60 * 60) % 24;
        long days = remaining / (1000 * 60 * 60 * 24);

        return message
                .replace("%status%", remaining > 0 ? "§cOn Cooldown" : "§aReady")
                .replace("%day%", String.valueOf(days))
                .replace("%hour%", String.valueOf(hours))
                .replace("%minute%", String.valueOf(minutes))
                .replace("%sec%", String.valueOf(seconds));
    }

    public static String parseTimeLeft(String message) {
        MarketManager manager = SinNightMarket.getInstance().getMarketManager();
        LocalTime now = LocalTime.now();
        LocalTime start = manager.getStartTimeLocal();
        LocalTime end = manager.getEndTimeLocal();
        long secondsLeft;

        if (manager.isMarketOpen()) {
            secondsLeft = java.time.Duration.between(now, end).getSeconds();
            message = message.replace("%market_status%", "Open");
        } else {
            secondsLeft = java.time.Duration.between(now, start).getSeconds();
            message = message.replace("%market_status%", "Closed");
        }

        long sec = secondsLeft % 60;
        long min = (secondsLeft / 60) % 60;
        long hour = (secondsLeft / 3600) % 24;
        long day = (secondsLeft / 86400);

        return message.replace("%day%", String.valueOf(day))
                .replace("%hour%", String.valueOf(hour))
                .replace("%minute%", String.valueOf(min))
                .replace("%sec%", String.valueOf(sec));
    }
}
