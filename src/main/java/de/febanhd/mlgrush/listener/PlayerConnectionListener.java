package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
            player.setFoodLevel(40);
            player.setMaxHealth(20);
            player.setHealth(player.getMaxHealth());
            player.teleport(MLGRush.getInstance().getGameHandler().getLobbyHandler().getLobbyLocation());
        }, 3);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        GameSession gameSession = MLGRush.getInstance().getGameHandler().getSessionByPlayer(player);
        if(gameSession != null && gameSession.isSelectingWorld()) {
            gameSession.setSelectingWorld(false);
            gameSession.getPlayer1().closeInventory();
            gameSession.getPlayer2().closeInventory();
        }
        if(gameSession != null) {
            gameSession.stopGame(null);
        }
    }
}
