package de.febanhd.mlgrush.stats;

import de.febanhd.mlgrush.util.UUIDFetcher;
import lombok.SneakyThrows;
import de.febanhd.sql.SimpleSQL;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.UUID;

public class StatsDataHandler {

    private SimpleSQL sqlHandler;

    public StatsDataHandler(SimpleSQL sqlHandler) {
        this.sqlHandler = sqlHandler;
    }

    public void setValueAsync(String rowName, UUID uuid, Object value) {
        sqlHandler.createBuilder("UPDATE mlg_stats SET `" + rowName + "`=? WHERE UUID=?").addObjects(value, uuid.toString()).updateAsync();
    }

    public void setValueSync(String rowName, UUID uuid, Object value) {
        sqlHandler.createBuilder("UPDATE mlg_stats SET `" + rowName + "`=? WHERE UUID=?").addObjects(value, uuid.toString()).updateSync();
    }

    @SneakyThrows
    public int getKills(UUID uuid) {
        int kills = 0;
        ResultSet rs = sqlHandler.createBuilder("SELECT kills FROM `mlg_stats` WHERE UUID=?").addObjects(uuid.toString()).querySync();
        if(rs.next()) {
            kills = rs.getInt("kills");
        }
        return kills;
    }

    @SneakyThrows
    public int getDeaths(UUID uuid) {
        int deaths = 0;
        ResultSet rs = sqlHandler.createBuilder("SELECT deaths FROM `mlg_stats` WHERE UUID=?").addObjects(uuid.toString()).querySync();
        if(rs.next()) {
            deaths = rs.getInt("deaths");
        }
        return deaths;
    }

    @SneakyThrows
    public int getWins(UUID uuid) {
        int wins = 0;
        ResultSet rs = sqlHandler.createBuilder("SELECT wins FROM `mlg_stats` WHERE UUID=?").addObjects(uuid.toString()).querySync();
        if(rs.next()) {
            wins = rs.getInt("deaths");
        }
        return wins;
    }

    @SneakyThrows
    public int getLooses(UUID uuid) {
        int looses = 0;
        ResultSet rs = sqlHandler.createBuilder("SELECT looses FROM `mlg_stats` WHERE UUID=?").addObjects(uuid.toString()).querySync();
        if(rs.next()) {
            looses = rs.getInt("looses");
        }
        return looses;
    }

    @SneakyThrows
    public PlayerStats getPlayerStats(Player player) {
        UUID uuid = UUIDFetcher.getUUID(player.getName());
        ResultSet rs = sqlHandler.createBuilder("SELECT * FROM `mlg_stats` WHERE UUID=?").addObjects(uuid.toString()).querySync();
        if(rs.next()) {
            int kills = rs.getInt("kills");
            int deaths = rs.getInt("deaths");
            int looses = rs.getInt("looses");
            int wins = rs.getInt("wins");
            int beds = rs.getInt("beds");
            return new PlayerStats(player, uuid, kills, deaths, wins, looses, beds);
        }

        return null;
    }

    @SneakyThrows
    public boolean hasStats(UUID uuid) {
        ResultSet rs = sqlHandler.createBuilder("SELECT * FROM `mlg_stats` WHERE UUID=?").addObjects(uuid.toString()).querySync();
        return rs.next();
    }

    public void insert(UUID uuid) {
        this.sqlHandler.createBuilder("INSERT INTO `mlg_stats` (UUID, kills, deaths, wins, looses, beds) VALUES (?,?,?,?,?,?)").addObjects(uuid.toString(), 0, 0, 0, 0, 0).updateSync();
    }

}
