package de.febanhd.mlgrush.commands;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            if(!MLGRush.getInstance().getGameHandler().isInSession(player)) {
                player.sendMessage(MLGRush.getMessage("messages.not_in_round"));
                return false;
            }
            GameSession session = MLGRush.getInstance().getGameHandler().getSessionByPlayer(player);
            session.stopGame(null, player);
            player.sendMessage(MLGRush.getMessage("messages.leave_round"));
        }

        return false;
    }
}
