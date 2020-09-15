package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.inventory.MapChoosingGui;
import de.febanhd.mlgrush.map.MapTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInv = event.getClickedInventory();
        Inventory openInv = event.getWhoClicked().getOpenInventory().getTopInventory();
        Player player = (Player)event.getWhoClicked();
        ItemStack stack = event.getCurrentItem();
        if(stack == null || stack.getType() == Material.AIR) return;
        if(openInv.getTitle().equals(MapChoosingGui.GUI_NAME)) {
            event.setCancelled(true);
            if(openInv.equals(clickedInv)) {
                GameSession gameSession = MLGRush.getInstance().getGameHandler().getSessionByPlayer(player);
                if(gameSession == null) {
                    player.closeInventory();
                }else if(stack.getType() == Material.PAPER) {
                    MapTemplate mapTemplate = MLGRush.getInstance().getMapManager().getMapTemplate(ChatColor.stripColor(stack.getItemMeta().getDisplayName()));
                    if(mapTemplate != null) {
                        gameSession.setMapTemplate(mapTemplate);
                        gameSession.closeInv();
                    }
                }else if(stack.getType() == Material.BARRIER) {
                    gameSession.cancelMapChoosing();
                }

            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if(MLGRush.getInstance().getGameHandler().getSessionByPlayer(player) != null) {
            if(MLGRush.getInstance().getGameHandler().getSessionByPlayer(player).isSelectingWorld()) {
                Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
                    new MapChoosingGui().open(player);
                }, 3);
            }
        }
    }
}
