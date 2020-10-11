package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.setup.MapSetupSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPTemplateCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!player.hasPermission("mlgrush.tptemplate")) {
                player.sendMessage(MLGRush.getMessage("nopermissions"));
                return false;
            }
            MLGRush.getInstance().getMapTemplateWorld().teleportPlayer(player);
            player.sendMessage("§aTeleportiert...");
        }

        return false;
    }
}
