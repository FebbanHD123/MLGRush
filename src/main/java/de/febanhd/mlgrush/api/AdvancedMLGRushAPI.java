package de.febanhd.mlgrush.api;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.stats.PlayerStats;
import de.febanhd.mlgrush.stats.StatsCach;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class AdvancedMLGRushAPI {

    public PlayerStats getStats(final UUID uuid) {
        if(StatsCach.contains(uuid)) {
            return StatsCach.getStats(uuid);
        }else {
            return MLGRush.getInstance().getStatsDataHandler().getPlayerStats(uuid);
        }
    }

    public void getStatsAsync(final UUID uuid, final Consumer<PlayerStats> callback) {
        MLGRush.getExecutorService().execute(() -> {
            callback.accept(this.getStats(uuid));
        });
    }

    public Collection<MapTemplate> getMapTemplates() {
        return Lists.newArrayList(MLGRush.getInstance().getMapManager().getTemplates());
    }

    public MapTemplate getMapTemplate(String templateName) {
        return MLGRush.getInstance().getMapManager().getMapTemplate(templateName);
    }

    public Collection<GameSession> getRunningGames() {
        ArrayList<GameSession> sessions = Lists.newArrayList();
        MLGRush.getInstance().getGameHandler().getGameSessions().forEach(gameSession -> {
            if(gameSession.isIngame()) sessions.add(gameSession);
        });
        return sessions;
    }

    public GameSession getGame(Player player) {
        return MLGRush.getInstance().getGameHandler().getSessionByPlayer(player);
    }

    public boolean isInGame(Player player) {
        return MLGRush.getInstance().getGameHandler().isInSession(player);
    }

    public Entity getQueueEntity() {
        return MLGRush.getInstance().getGameHandler().getLobbyHandler().getQueueEntity();
    }

    public Executor getExecutorService() {
        return MLGRush.getExecutorService();
    }
}
