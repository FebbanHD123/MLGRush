package de.febanhd.mlgrush.inventory;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MapChoosingGui {

    public static final String GUI_NAME = "§eWähle deine Map";

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9 * 6, GUI_NAME);
        for(MapTemplate mapTemplate : MLGRush.getInstance().getMapManager().getTemplates()) {
            inv.addItem(new ItemBuilder(Material.PAPER).setDisplayName("§e" + mapTemplate.getName()).build());
        }
        inv.setItem(53, new ItemBuilder(Material.BARRIER).setDisplayName("§cAbbrechen").build());
        player.openInventory(inv);
    }
}
