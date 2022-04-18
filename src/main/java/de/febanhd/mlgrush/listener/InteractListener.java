package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import de.febanhd.mlgrush.game.spectator.SpectatorHandler;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import de.febanhd.mlgrush.gui.SpectatorGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(event.getRightClicked().getCustomName() != null && event.getRightClicked().getCustomName().equals(LobbyHandler.queueEntityName) && !MLGRush.getInstance().getGameHandler().isInSession(event.getPlayer())) {
            event.setCancelled(true);
            MLGRush.getInstance().getGameHandler().toggleQueue(event.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack stack = event.getItem();
        Player player = event.getPlayer();
        if(stack != null && stack.getType() != Material.AIR) {
            if(!MLGRush.getInstance().getGameHandler().isInSession(player)) {
                if(stack.getType().equals(Material.CHEST) && (stack.hasItemMeta() && stack.getItemMeta().getDisplayName().equals(InventorySortingGui.GUI_NAME))) {
                    player.chat("/sortinv");
                    event.setCancelled(true);
                } else if(stack.equals(MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler().getSpectatorItem())) {
                    new SpectatorGui().open(player);
                    event.setCancelled(true);
                } else {
                    SpectatorHandler spectatorHandler = MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler();
                    if(spectatorHandler.isSpectating(player) && stack.getType().equals(Material.BARRIER)) {
                        spectatorHandler.cancelSpectating(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals(LobbyHandler.queueEntityName)) event.setCancelled(true);
    }
}
