package de.febanhd.mlgrush.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.nms.NMSUtil;
import de.febanhd.mlgrush.util.Materials;
import de.febanhd.mlgrush.util.SkullBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class SpectatorGui {

    public static String GUI_NAME = "§5Spectate Players";

    public void open(Player player) {
        ArrayList<Player> players = Lists.newArrayList();
        MLGRush.getInstance().getGameHandler().getGameSessions().forEach(sessions -> {
            if(sessions.isIngame()) {
                players.add(sessions.getPlayer1());
                players.add(sessions.getPlayer2());
            }
        });

        Inventory inventory = Bukkit.createInventory(null, 9 * 6, GUI_NAME);
        for(int i = 0; i < players.size() && i < inventory.getSize(); i++) {
            Player target = players.get(i);
            GameSession session = MLGRush.getInstance().getGameHandler().getSessionByPlayer(target);

            ItemStack stack = Materials.PLAYER_HEAD.getStack().build();

            SkullMeta meta = (SkullMeta) stack.getItemMeta();
            meta.setOwner(target.getName());
            meta.setDisplayName("§e" + target.getDisplayName());
            meta.setLore(Arrays.asList("§7Map: §e" + session.getMapTemplate().getName(), "§7GameID: §e" + session.getId()));
            stack.setItemMeta(meta);

            inventory.setItem(i, stack);
        }
        player.openInventory(inventory);
    }
}
