package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.setup.MapSetupSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupMapCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {

            Player player = (Player) sender;

            if(!player.hasPermission("mlgrush.setupmap")) {
                player.sendMessage(MLGRush.getMessage("nopermissions"));
                return false;
            }

            if(!MLGRush.getInstance().getMapTemplateWorld().getWorld().equals(player.getWorld())) {
                player.sendMessage("§cDu bist nicht in der Template Welt. Benutze §6'/tptemplate' §cum in diese Welt zu gelangen!");
                return false;
            }

            if(!MapSetupSession.isInSetup(player)) {
                new MapSetupSession(player);
            }else {
                player.sendMessage(MLGRush.getMessage("messages.already_in_setup"));
            }
        }

        return false;
    }
}
