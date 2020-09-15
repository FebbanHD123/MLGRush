package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player)sender;
            if(!player.hasPermission("mlgrush.setlobby")) {
                player.sendMessage(MLGRush.PREFIX + "§cDazu hast du keine Rechte!");
                return false;
            }
            MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyLocation(player.getLocation());
            player.sendMessage(MLGRush.PREFIX + "§2Lobby-Spawn gesetzt.");
        }
        return false;
    }
}
