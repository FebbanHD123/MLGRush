package de.febanhd.mlgrush.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import de.febanhd.mlgrush.game.lobby.LobbyQueue;
import de.febanhd.mlgrush.util.Sounds;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameHandler {

    @Getter
    private final CopyOnWriteArrayList<GameSession> gameSessions = Lists.newCopyOnWriteArrayList();
    private HashMap<UUID, Long> lastClicked = Maps.newHashMap();

    @Getter
    private final LobbyQueue queue;
    @Getter
    private final LobbyHandler lobbyHandler;

    private HashMap<Player, Player> challangerMap = Maps.newHashMap();

    public GameHandler() {
        this.queue = new LobbyQueue();
        this.lobbyHandler = new LobbyHandler(this);
    }

    public void toggleQueue(Player player) {
        player.playSound(player.getLocation(), Sounds.LEVEL_UP.getSound(), 2, 1);
        if(this.lastClicked.containsKey(player.getUniqueId())) {
            if(this.lastClicked.get(player.getUniqueId()) + 200 > System.currentTimeMillis()) {
                this.lastClicked.put(player.getUniqueId(), System.currentTimeMillis());
                return;
            }
        }
        if(this.queue.isInQueue(player)) {
            queue.remove(player);
            player.sendMessage(MLGRush.getMessage("messages.queue.quit"));
        }else {
            this.addToQueue(player);
            player.sendMessage(MLGRush.getMessage("messages.queue.enter"));
        }
        lastClicked.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void addToQueue(Player player) {
        this.queue.add(player);
        if (this.queue.isFull()) {
            Player player1 = queue.getPlayer(0);
            Player player2 = queue.getPlayer(1);
            this.queue.clear();
            this.removeChallangerFromMap(player1);
            this.removeChallangerFromMap(player2);
            this.gameSessions.add(new GameSession(player1, player2));
        }
    }

    public GameSession getSessionByPlayer(Player player) {
        for(GameSession gameSession : this.gameSessions) {
            if(gameSession.getPlayer1().equals(player) || gameSession.getPlayer2().equals(player)) {
                return gameSession;
            }
        }
        return null;
    }

    public GameSession getSessionByID(String id) {
        for(GameSession gameSession : this.gameSessions) {
            if(gameSession.getId().equals(id)) {
                return gameSession;
            }
        }
        return null;
    }

    public boolean isInSession(Player player) {
        return this.getSessionByPlayer(player) != null;
    }

    public void setTarget(Player player, Player target) {
        this.challangerMap.put(player, target);
        if(this.hasTarget(target) && this.getTarget(target).equals(player)) {
            if(this.queue.getPlayers().contains(player))
                this.queue.remove(player);
            if(this.queue.getPlayers().contains(target))
                this.queue.remove(target);

            this.removeChallangerFromMap(player);
            this.removeChallangerFromMap(target);

            this.gameSessions.add(new GameSession(player, target));
        }
    }

    public Player getTarget(Player player) {
        return this.challangerMap.get(player);
    }

    public boolean hasTarget(Player player) {
        return this.challangerMap.containsKey(player);
    }

    public void removeChallangerFromMap(Player player) {
        if(this.challangerMap.containsKey(player))
            this.challangerMap.remove(player);
    }
}
