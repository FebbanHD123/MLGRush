package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.setup.MapSetupSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MLGRushCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(!player.hasPermission("mlgrush.setlobby") && !player.hasPermission("mlgrush.setqueue") && !player.hasPermission("mlgrush.setupmap")) {
                player.sendMessage(MLGRush.PREFIX + "§6AdvancedMLGRush V. " + MLGRush.getInstance().getDescription().getVersion() + " by §eFebanHD");
                return false;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("setlobby")) {
                    if (!player.hasPermission("mlgrush.setlobby")) {
                        player.sendMessage(MLGRush.getMessage("nopermissions"));
                        return false;
                    }
                    MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyLocation(player.getLocation());
                    player.sendMessage(MLGRush.PREFIX + "§2Lobby-Spawn gesetzt.");
                } else if (args[0].equalsIgnoreCase("setqueue")) {
                    if (!player.hasPermission("mlgrush.setqueue")) {
                        player.sendMessage(MLGRush.getMessage("nopermissions"));
                        return false;
                    }
                    MLGRush.getInstance().getGameHandler().getLobbyHandler().setQueueEntityLocation(player.getLocation());
                    player.sendMessage(MLGRush.PREFIX + "§2Queue gesetzt.");
                } else if (args[0].equalsIgnoreCase("setupmap")) {
                    if (!player.hasPermission("mlgrush.setupmap")) {
                        player.sendMessage(MLGRush.getMessage("nopermissions"));
                        return false;
                    }

                    if (!MLGRush.getInstance().getMapTemplateWorld().getWorld().equals(player.getWorld())) {
                        player.sendMessage("§cDu bist nicht in der Template Welt. Benutze §6'/tptemplate' §cum in diese Welt zu gelangen!");
                        return false;
                    }

                    if (!MapSetupSession.isInSetup(player)) {
                        new MapSetupSession(player);
                    } else {
                        player.sendMessage(MLGRush.getMessage("messages.already_in_setup"));
                    }
                }else
                    sendUsage(player);
            } else {
                sendUsage(player);
            }
        }else
            sender.sendMessage("§cOnly for players!");
        return false;
    }

    private void sendUsage(Player player) {
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush setlobby §7- Setzt die lobby");
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush setqueue §7- Setzt die queue/warteschlange");
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush setupmap §7- Starte ein Map-Setup");
    }
    ///mlgrush setlobby
}
