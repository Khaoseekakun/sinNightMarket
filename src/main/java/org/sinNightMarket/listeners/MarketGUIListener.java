package org.sinNightMarket.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.sinNightMarket.market.MarketGUI;
import org.sinNightMarket.market.ItemStorage;

public class MarketGUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        MarketGUI gui = MarketGUI.getOpenGUI(player);
        if (gui == null) return;

        if (!event.getView().getTitle().equals(MarketGUI.GUI_TITLE)) return;

        event.setCancelled(true); // prevent dragging

        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ItemStorage item = gui.getSlotMap().get(slot);
        if (item == null) return;

        gui.purchaseItem(player, item, slot);
    }
}
