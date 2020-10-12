package de.febanhd.mlgrush.stats;

import de.febanhd.mlgrush.MLGRush;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PlayerStats {

    private final UUID uuid;
    private int kills, deaths, wins, looses, bedDestroyed;
    private final StatsDataHandler statsDataHandler;

    public PlayerStats(UUID uuid, int kills, int deaths, int wins, int looses, int bedDestroyed) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.wins = wins;
        this.looses = looses;
        this.statsDataHandler = MLGRush.getInstance().getStatsDataHandler();
        this.bedDestroyed = bedDestroyed;
    }

    public void addKill() {
        kills++;
        this.statsDataHandler.setValueAsync("kills", uuid, kills);
    }

    public void addDeaths() {
        deaths++;
        this.statsDataHandler.setValueAsync("deaths", uuid, deaths);
    }

    public void addWin() {
        wins++;
        this.statsDataHandler.setValueAsync("wins", uuid, this.wins);
    }

    public void addLoose() {
        looses++;
        this.statsDataHandler.setValueAsync("looses", uuid, this.looses);
    }

    public void addBedDestroyed() {
        this.bedDestroyed++;
        this.statsDataHandler.setValueAsync("beds", uuid, this.bedDestroyed);
    }
}
