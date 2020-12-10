package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MLGRushCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        sender.sendMessage(MLGRush.PREFIX + "§cAdvancedMLGRush by §4FebanHD");
        sender.sendMessage(MLGRush.PREFIX + "§7You can Download i");
        return false;
    }
}
