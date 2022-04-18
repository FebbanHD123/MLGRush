package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.inventorysorting.InventorySorting;
import de.febanhd.mlgrush.game.inventorysorting.InventorySortingCach;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SortInvCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if(MLGRush.getInstance().getGameHandler().isInSession(player)) {
                    player.sendMessage(MLGRush.getMessage("messages.inventorysorting.error_in_round"));
                    return false;
                }
                InventorySorting sorting = InventorySortingCach.getSorting(player);
                if (sorting == null) {
                    player.sendMessage(MLGRush.PREFIX + "§cDeine Inventarsortierung konnte nicht geladen werden!");
                    return false;
                }
                new InventorySortingGui().open(player, sorting);
            }
        }
        return false;
    }
}
