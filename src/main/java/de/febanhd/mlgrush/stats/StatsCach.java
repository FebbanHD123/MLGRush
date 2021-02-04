package de.febanhd.mlgrush.stats;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.UUIDFetcher;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class StatsCach {

    private static CopyOnWriteArrayList<PlayerStats> playerStats = Lists.newCopyOnWriteArrayList();

    public static void loadStats(Player player) {
        UUIDFetcher.getUUID(player.getName(), uuid -> {
            StatsDataHandler statsHandler = MLGRush.getInstance().getStatsDataHandler();
            if(!statsHandler.hasStats(uuid)) {
                statsHandler.insert(uuid);
            }
            playerStats.add(statsHandler.getPlayerStats(player));
        });
    }

    public static PlayerStats getStats(Player player) {
        for(PlayerStats stats : StatsCach.playerStats) {
            if(stats.getPlayer().equals(player)) {
                return stats;
            }
        }
        return new PlayerStats(null, UUID.randomUUID(), 0, 0, 0, 0, 0);
    }

    public static void remove(Player player) {
        PlayerStats stats = getStats(player);
        playerStats.remove(stats);
    }

    public static boolean contains(Player player) {
        for(PlayerStats stats : StatsCach.playerStats) {
            if(stats.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }
}
