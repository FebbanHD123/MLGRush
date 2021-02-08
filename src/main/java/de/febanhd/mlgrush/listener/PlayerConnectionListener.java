package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.game.lobby.LobbyQueue;
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
            player.getActivePotionEffects().forEach(potionEffect -> event.getPlayer().removePotionEffect(potionEffect.getType()));
        }, 3);

        StatsCach.loadStats(player);
        InventorySortingCach.loadSorting(player);

        if(player.hasPermission("mlgrush.notify") &&
                !MLGRush.getInstance().getUpdateChecker().getCachedVersion().equals(MLGRush.getInstance().getDescription().getVersion())) {
            player.sendMessage(MLGRush.PREFIX + "§7Es gibt eine neuere Version von AdvancedMLGRush (" + MLGRush.getInstance().getUpdateChecker().getCachedVersion() +
                    "). Downloade diese hier: §nhttps://www.spigotmc.org/resources/mlgrush-%E2%9C%85-the-most-advanced-mlgrush-system-unendlich-maps-mit-nur-einem-template.84672/");
        }

        Bukkit.getOnlinePlayers().forEach(players -> {
            if(MLGRush.getInstance().getGameHandler().isInSession(players) && MLGRush.getInstance().getGameHandler().getSessionByPlayer(players).isIngame()) {
                players.hidePlayer(player);
            }
        });
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

        Bukkit.getOnlinePlayers().forEach(players -> {
            if(spectatorHandler.isSpectating(players)) {
                player.hidePlayer(players);
            }
        });

        MLGRush.getInstance().getGameHandler().removeChallangerFromMap(player);
        LobbyQueue lobbyQueue = MLGRush.getInstance().getGameHandler().getQueue();
        if(lobbyQueue.isInQueue(player)) {
            lobbyQueue.remove(player);
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(MLGRush.getInstance(), () -> {
            if(!player.isOnline()) {
                StatsCach.remove(player);
                InventorySortingCach.remove(player);
            }
        }, 10);
    }
}
