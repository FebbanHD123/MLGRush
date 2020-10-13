package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySorting;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingCach;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import de.febanhd.mlgrush.gui.MapChoosingGui;
import de.febanhd.mlgrush.gui.SpectatorGui;
import de.febanhd.mlgrush.map.MapTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
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
        }else if (openInv.getTitle().equals(InventorySortingGui.GUI_NAME)) {
            if(!openInv.equals(clickedInv)) {
                event.setCancelled(true);
            }
        }else if (openInv.getTitle().equals(SpectatorGui.GUI_NAME)) {
            event.setCancelled(true);
            if(!stack.hasItemMeta()) return;
            String dpName = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
            Player target = this.getPlayerByDisplayName(dpName);
            if(target == null || !MLGRush.getInstance().getGameHandler().isInSession(target)) {
                player.closeInventory();
                player.sendMessage(MLGRush.getMessage("messages.lobby.is_not_in_round"));
                return;
            }
            MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler().spectate(player, target);
            player.closeInventory();
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
        }else if(event.getInventory().getTitle().equals(InventorySortingGui.GUI_NAME)) {
            InventorySorting sorting = InventorySortingCach.getSorting((Player) event.getPlayer());
            sorting.updateItems(event.getInventory(), correct -> {
                if(correct) {
                    player.sendMessage(MLGRush.getMessage("messages.inventorysorting.succesfully"));
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, 1);
                }else {
                    player.sendMessage(MLGRush.getMessage("messages.inventorysorting.error"));
                }
                event.getPlayer().getInventory().clear();
                MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player);
            });
        }
    }

    private Player getPlayerByDisplayName(String dpName) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getDisplayName().equals(dpName)) {
                return player;
            }
        }
        return null;
    }
}
