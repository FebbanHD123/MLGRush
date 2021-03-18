package de.febanhd.mlgrush.gui;

import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySorting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InventorySortingGui {

    public static String GUI_NAME = "§eInventory Sorting";

    public void open(Player player, InventorySorting sorting) {
        Inventory inventory = Bukkit.createInventory(null, 9, GUI_NAME);
        sorting.setToInventory(inventory);
        player.openInventory(inventory);
    }
}
