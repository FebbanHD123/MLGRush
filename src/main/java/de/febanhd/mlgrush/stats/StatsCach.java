package de.febanhd.mlgrush.stats;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class StatsCach {

    private static CopyOnWriteArrayList<PlayerStats> playerStats = Lists.newCopyOnWriteArrayList();

    public static void loadStats(UUID uuid) {
        MLGRush.getExecutorService().execute(() -> {
            StatsDataHandler statsHandler = MLGRush.getInstance().getStatsDataHandler();
            if(!statsHandler.hasStats(uuid)) {
                statsHandler.insert(uuid);
            }
            playerStats.add(statsHandler.getPlayerStats(uuid));
        });
    }

    public static PlayerStats getStats(UUID uuid) {
        for(PlayerStats stats : StatsCach.playerStats) {
            if(stats.getUuid().equals(uuid)) {
                return stats;
            }
        }
        return null;
    }

    public static void remove(UUID uuid) {
        PlayerStats stats = getStats(uuid);
        playerStats.remove(stats);
    }
}
