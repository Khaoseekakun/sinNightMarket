package org.sinNightMarket.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.sinNightMarket.SinNightMarket;
import org.sinNightMarket.market.MarketGUI;
import org.sinNightMarket.market.MarketManager;
import org.sinNightMarket.market.ItemStorage;
import org.sinNightMarket.utils.Utils;

@SuppressWarnings("CallToPrintStackTrace")
public class NightMarketCommand implements CommandExecutor {

    private final SinNightMarket plugin = SinNightMarket.getInstance();
    private final MarketManager marketManager = plugin.getMarketManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        try {
            if (args.length == 0) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Utils.color("&cOnly players can open the market."));
                    return true;
                }

                if (!marketManager.isMarketOpen()) {
                    sender.sendMessage(Utils.parseMessage(player,"market-closed"));
                    return true;
                }

                new MarketGUI(player).open();
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "additem" -> {
                    try {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(Utils.color("&cOnly players can add items."));
                            return true;
                        }
                        if (!player.hasPermission("nightmarket.admin")) {
                            sender.sendMessage(Utils.color("&cYou do not have permission."));
                            return true;
                        }
                        if (args.length != 5) {
                            sender.sendMessage(Utils.color("&cUsage: /nightmarket additem <chance> <stockMin> <stockMax> <price>"));
                            return true;
                        }

                        ItemStack handItem = player.getInventory().getItemInMainHand();
                        if (handItem.getType().isAir()) {
                            sender.sendMessage(Utils.color("&cHold an item to add."));
                            return true;
                        }

                        double chance = Double.parseDouble(args[1]);
                        int min = Integer.parseInt(args[2]);
                        int max = Integer.parseInt(args[3]);
                        double price = Double.parseDouble(args[4]);

                        ItemStorage storage = new ItemStorage(handItem, chance, min, max, price);
                        marketManager.addItem(storage);
                        sender.sendMessage(Utils.parseMessage(player,"item-added", "%item%", handItem.getType().toString()));

                    } catch (NumberFormatException e) {
                        sender.sendMessage(Utils.color("&cInvalid number format."));
                    } catch (Exception e) {
                        sender.sendMessage(Utils.color("&cError adding item: " + e.getMessage()));
                        e.printStackTrace();
                    }
                }
                case "reload" -> {
                    try {
                        if (!sender.hasPermission("nightmarket.admin")) {
                            sender.sendMessage(Utils.color("&cYou do not have permission."));
                            return true;
                        }
                        // Call onDisable then onEnable
                        this.plugin.onDisable();
                        this.plugin.onEnable();
                        sender.sendMessage(Utils.color("&aNightMarket reloaded successfully."));
                    } catch (Exception e) {
                        sender.sendMessage(Utils.color("&cError reloading NightMarket: " + e.getMessage()));
                        e.printStackTrace();
                    }
                }
                default -> sender.sendMessage(Utils.color("&cUnknown subcommand."));
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.color("&cAn unexpected error occurred: " + e.getMessage()));
            e.printStackTrace();
        }
        return true;
    }
}
