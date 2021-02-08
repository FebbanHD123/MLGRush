package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.map.setup.MapSetupSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class MLGRushCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(!player.hasPermission("mlgrush.setlobby") && !player.hasPermission("mlgrush.setqueue") && !player.hasPermission("mlgrush.setupmap") && !player.hasPermission("mlgrush.deletemap")) {
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
                }else
                    sendUsage(player);
            } else if (args.length == 2) {
                if(args[0].equalsIgnoreCase("setupmap")) {
                    if (!player.hasPermission("mlgrush.setupmap")) {
                        player.sendMessage(MLGRush.getMessage("nopermissions"));
                        return false;
                    }
                    if (args[1].equalsIgnoreCase("set")) {
                        if (MapSetupSession.isInSetup(player)) {
                            MapSetupSession.PLAYERS.get(player).doSet();
                        }
                    } else {
                        if (MapSetupSession.isInSetup(player)) {
                            player.sendMessage(MLGRush.getMessage("messages.already_in_setup"));
                        }
                        if (!MLGRush.getInstance().getMapTemplateWorld().getWorld().equals(player.getWorld())) {
                            player.sendMessage("§fGerman: §cDu bist nicht in der Template Welt. Benutze §6'/tptemplate' §cum in diese Welt zu gelangen!");
                            player.sendMessage("§fEnglish: §cYou are not in the template world. Use §6'/tptemplate' §cto get into this world!");
                            return false;
                        }
                        String languageKey = args[1];
                        boolean english;
                        if (languageKey.equalsIgnoreCase("de"))
                            english = false;
                        else if (languageKey.equalsIgnoreCase("en"))
                            english = true;
                        else {
                            player.sendMessage(MLGRush.PREFIX + "§fGerman: §cDiese Sprache ist nicht verügbar! Verfügbar sind: §e\"DE\" §7oder §e\"'EN'\"§7.");
                            player.sendMessage(MLGRush.PREFIX + "§fEnglish: §cThis language is not available! Available: §e\\\"DE\\\" §7or §e\\\"'EN'\\\"§7.\"");
                            return false;
                        }
                        new MapSetupSession(player, english);
                    }
                }else if(args[0].equalsIgnoreCase("deletemap")) {
                    if (!player.hasPermission("mlgrush.deletemap")) {
                        player.sendMessage(MLGRush.getMessage("nopermissions"));
                        return false;
                    }
                    String templateName = args[1];
                    MapTemplate template = MLGRush.getInstance().getMapManager().getMapTemplate(templateName);
                    if(template != null) {
                        File file = MLGRush.getInstance().getMapManager().getMapTemplateStorage().getFileFromTemplate(template);
                        file.delete();
                        player.sendMessage(MLGRush.PREFIX + "§aMap gelöscht.");
                    }else {
                        player.sendMessage(MLGRush.PREFIX + "§cDieses Template existiert nicht!");
                    }

                }
            }else if (args.length == 3 && args[0].equalsIgnoreCase("setupmap") && args[1].equalsIgnoreCase("setname")) {
                if(MapSetupSession.isInSetup(player)) {
                    String name = args[2];
                    MapSetupSession.PLAYERS.get(player).setName(name);
                }
            }else {
                sendUsage(player);
            }
        }else
            sender.sendMessage("§cOnly for players!");
        return false;
    }

    private void sendUsage(Player player) {
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush setlobby §7- Setzt die lobby");
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush setqueue §7- Setzt die queue/warteschlange");
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush setupmap <DE/EN> §7- Starte ein Map-Setup");
        player.sendMessage(MLGRush.PREFIX + "§e/mlgrush deletemap <Mapname> §7- Lösche eine Map");
    }
    ///mlgrush setlobby
    // /mlgrush setupmap set
    // /mlgrush setupmap <languadge (DE/EN)>
    // /mlgrush deletemap <MapName>
}
