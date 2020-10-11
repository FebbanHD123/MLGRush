package de.febanhd.mlgrush.game;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import de.febanhd.mlgrush.game.lobby.LobbyQueue;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;

public class GameHandler {

    @Getter
    private final CopyOnWriteArrayList<GameSession> gameSessions = Lists.newCopyOnWriteArrayList();

    @Getter
    private final LobbyQueue queue;
    @Getter
    private LobbyHandler lobbyHandler;

    public GameHandler() {
        this.queue = new LobbyQueue();
        this.lobbyHandler = new LobbyHandler(this);
    }

    public void toggleQueue(Player player) {
        if(this.queue.isInQueue(player)) {
            queue.remove(player);
            player.sendMessage(MLGRush.getMessage("messages.queue.quit"));
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 2, 1);
        }else {
            this.addToQueue(player);
            player.sendMessage(MLGRush.getMessage("messages.queue.enter"));
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 3, 2);
        }
    }

    private void addToQueue(Player player) {
        this.queue.add(player);
        if (this.queue.isFull()) {
            Player player1 = queue.getPlayer(0);
            Player player2 = queue.getPlayer(1);
            this.queue.clear();
            this.gameSessions.add(new GameSession(player1, player2, 15));
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

    public boolean isInSession(Player player) {
        return this.getSessionByPlayer(player) != null;
    }
}
