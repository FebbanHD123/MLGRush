package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class InteractListener implements Listener {

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(event.getRightClicked().getCustomName().equals(LobbyHandler.QUEUE_ENTITY_NAME)) {
            event.setCancelled(true);
            MLGRush.getInstance().getGameHandler().toggleQueue(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals(LobbyHandler.QUEUE_ENTITY_NAME)) event.setCancelled(true);
    }
}
