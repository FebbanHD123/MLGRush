package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.setup.MapSetupSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateMapTemplateCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!player.hasPermission("mlgrush.setup")) {
                player.sendMessage(MLGRush.PREFIX + "§cDazu hast du keine Rechte!");
                return false;
            }

            new MapSetupSession(player);

        }

        return false;
    }
}
