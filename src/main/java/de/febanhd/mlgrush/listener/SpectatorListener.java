package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class SpectatorListener implements Listener {

    private boolean isSpectator(Player player) {
        return MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler().isSpectating(player);
    }

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if(this.isSpectator(player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(this.isSpectator(player)) {
            if(player.getLocation().getY() < 0) {
                player.teleport(MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler().getTarget(player));
            }
        }
    }
}
