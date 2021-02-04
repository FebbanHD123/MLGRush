package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.stats.PlayerStats;
import de.febanhd.mlgrush.stats.StatsCach;
import de.febanhd.mlgrush.stats.StatsDataHandler;
import de.febanhd.mlgrush.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0) {
            if(!sender.hasPermission("mlgrush.stats")) {
                sender.sendMessage(MLGRush.getMessage("nopermissions"));
                return false;
            }
            if(sender instanceof Player) {
                Player player = (Player)sender;
                PlayerStats stats = StatsCach.getStats(player);
                if(stats == null) {
                    player.sendMessage(MLGRush.PREFIX + "§cDeine Stats wurden noch nicht geladen!");
                    return false;
                }
                this.sendStats(player, stats, player.getName());
            }
        }else if (args.length == 1) {
            String targetName = args[0];
            if(sender instanceof Player && targetName.equalsIgnoreCase(sender.getName())) {
                ((Player)sender).chat("/stats");
                return true;
            }
            if(!sender.hasPermission("mlgrush.stats.others")) {
                sender.sendMessage(MLGRush.getMessage("nopermissions"));
                return false;
            }
            sender.sendMessage(MLGRush.getMessage("messages.stats.loading"));
            if(Bukkit.getPlayer(targetName) != null) {
                Player target = Bukkit.getPlayer(targetName);
                MLGRush.getExecutorService().execute(() -> {
                    StatsDataHandler statsDataHandler = MLGRush.getInstance().getStatsDataHandler();
                    if(!statsDataHandler.hasStats(UUIDFetcher.getUUID(target.getName()))) {
                        sender.sendMessage(MLGRush.getMessage("messages.stats.not_found"));
                        return;
                    }
                    this.sendStats(sender, statsDataHandler.getPlayerStats(target), targetName);
                });
            } else {

            }
        }
        return false;
    }

    private String getStatsMessage(PlayerStats stats, String playerName) {
        String message = MLGRush.getInstance().getConfig().getString("messages.stats.command");
        message = message.replaceAll("%player%", playerName);
        message = message.replaceAll("%kills%", String.valueOf(stats.getKills()));
        message = message.replaceAll("%deaths%", String.valueOf(stats.getDeaths()));
        message = message.replaceAll("%kd%", this.getKD(stats.getKills(), stats.getDeaths()));
        message = message.replaceAll("%wins%", String.valueOf(stats.getWins()));
        message = message.replaceAll("%looses%", String.valueOf(stats.getLooses()));
        message = message.replaceAll("%beds%", String.valueOf(stats.getBedDestroyed()));
        return message;
    }

    private void sendStats(CommandSender sender, PlayerStats stats, String playerName) {
        String message = this.getStatsMessage(stats, playerName);
        String[] args = message.split("%n%");
        for(int i = 0; i < args.length; i++) {
            sender.sendMessage(MLGRush.PREFIX + ChatColor.translateAlternateColorCodes('&', args[i]));
        }
    }

    private String getKD(int kills, int deaths) {
        double kd = (double)kills / (double)deaths;
        double roundedKD = Math.round(kd * 100.0) / 100.0;
        return String.valueOf(roundedKD);
    }
}
