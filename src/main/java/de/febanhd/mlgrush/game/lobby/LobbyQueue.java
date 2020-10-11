package de.febanhd.mlgrush.game.lobby;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class LobbyQueue {

    @Getter
    private ArrayList<Player> players = Lists.newArrayList();

    public Player getPlayer(int i) {
        return this.players.get(i);
    }

    public void add(Player player) {
        this.players.add(player);
    }
    public void remove(Player player) {
        this.players.remove(player);
    }

    public boolean isFull() {
        return this.players.size() >= 2;
    }

    public boolean isInQueue(Player player) {
        return this.players.contains(player);
    }
}
