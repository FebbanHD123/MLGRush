package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingCach;
import de.febanhd.mlgrush.game.lobby.spectator.SpectatorHandler;
import de.febanhd.mlgrush.map.setup.MapSetupSession;
import de.febanhd.mlgrush.stats.StatsCach;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
            player.getInventory().clear();
            if(!player.isOp())
                player.setGameMode(GameMode.ADVENTURE);
            MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player);

            Location lobbyLocation = MLGRush.getInstance().getGameHandler().getLobbyHandler().getLobbyLocation();
            if(lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }else {
                player.sendMessage(MLGRush.PREFIX + "§cBitte Setzte die Lobby-Position mit /setlobby!!!");
            }
        }, 3);

        StatsCach.loadStats(player.getUniqueId());
        InventorySortingCach.loadSorting(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if(MapSetupSession.isInSetup(player)) {
            MapSetupSession.PLAYERS.remove(player);
        }

        GameSession gameSession = MLGRush.getInstance().getGameHandler().getSessionByPlayer(player);
        if(gameSession != null && gameSession.isSelectingWorld()) {
            gameSession.setSelectingWorld(false);
            gameSession.getPlayer1().closeInventory();
            gameSession.getPlayer2().closeInventory();
        }
        if(gameSession != null && gameSession.isRunning()) {
            gameSession.stopGame(null, player);
        }

        SpectatorHandler spectatorHandler = MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler();
        if(spectatorHandler.isSpectating(player)) {
            spectatorHandler.cancelSpectating(player);
        }

        MLGRush.getInstance().getGameHandler().removeChallangerFromMap(player);

        Bukkit.getScheduler().runTaskLaterAsynchronously(MLGRush.getInstance(), () -> {
            if(!player.isOnline()) {
                StatsCach.remove(player.getUniqueId());
                InventorySortingCach.remove(player);
            }
        }, 10);
    }
}
