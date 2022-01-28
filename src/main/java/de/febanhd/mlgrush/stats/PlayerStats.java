package de.febanhd.mlgrush.stats;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.UUIDFetcher;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class PlayerStats {

    private final Player player;
    private final UUID uuid;
    private int kills, deaths, wins, looses, bedDestroyed;

    public PlayerStats(Player player, UUID uuid, int kills, int deaths, int wins, int looses, int bedDestroyed) {
        this.player = player;
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.wins = wins;
        this.looses = looses;
        this.bedDestroyed = bedDestroyed;
    }

    public void addKill() {
        kills++;
        MLGRush.getInstance().getStatsDataHandler().setValueAsync("kills", uuid, kills);
    }

    public void addDeaths() {
        deaths++;
        MLGRush.getInstance().getStatsDataHandler().setValueAsync("deaths", uuid, deaths);
    }

    public void addWin() {
        wins++;
        MLGRush.getInstance().getStatsDataHandler().setValueAsync("wins", uuid, this.wins);
    }

    public void addLoose() {
        looses++;
        MLGRush.getInstance().getStatsDataHandler().setValueAsync("looses", uuid, this.looses);
    }

    public void addBedDestroyed() {
        this.bedDestroyed++;
        MLGRush.getInstance().getStatsDataHandler().setValueAsync("beds", uuid, this.bedDestroyed);
    }

    public String getKD() {
        double kd = (double)kills / (double)deaths;
        double roundedKD = Math.round(kd * 100.0) / 100.0;
        return String.valueOf(roundedKD);
    }
}
