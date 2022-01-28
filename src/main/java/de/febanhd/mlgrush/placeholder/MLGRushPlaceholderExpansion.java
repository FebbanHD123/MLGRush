package de.febanhd.mlgrush.placeholder;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.api.AdvancedMLGRushAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MLGRushPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "advancedmlgrush";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FebanHD";
    }

    @Override
    public @NotNull String getVersion() {
        return MLGRush.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if(offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            AdvancedMLGRushAPI api = new AdvancedMLGRushAPI();
            switch (params.toLowerCase()) {
                case "kills":
                    return String.valueOf(api.getStats(player).getKills());
                case "deaths":
                    return String.valueOf(api.getStats(player).getDeaths());
                case "kd":
                    return api.getStats(player).getKD();
                case "wins":
                    return String.valueOf(api.getStats(player).getWins());
                case "looses":
                    return String.valueOf(api.getStats(player).getLooses());
                case "beds":
                    return String.valueOf(api.getStats(player).getBedDestroyed());
                default:
                    return super.onPlaceholderRequest(player, params);
            }
        }
        return null;
    }
}
