package de.febanhd.mlgrush.game.lobby.spectator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpectatorHandler {

    private ArrayList<Player> spectators = Lists.newArrayList();
    private HashMap<Player, Player> targetMap = Maps.newHashMap();

    public void spectate(Player player, Player target) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false), false);
        player.setAllowFlight(true);
        player.setFlying(true);
        spectators.add(player);
        this.targetMap.put(player, target);
        player.teleport(target.getLocation());
        this.setItems(player);
    }

    public void cancelSpectating(Player player) {
        this.spectators.remove(player);
        player.teleport(MLGRush.getInstance().getGameHandler().getLobbyHandler().getLobbyLocation());
        MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        this.targetMap.remove(player);
    }

    public void setItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(4, this.getSpectatorItem());
        player.getInventory().setItem(8, new ItemBuilder(Material.BARRIER).setDisplayName(MLGRush.getString("items.cancel")).build());
    }

    public ItemStack getSpectatorItem() {
        return new ItemBuilder(Material.EYE_OF_ENDER).setDisplayName(MLGRush.getString("items.spectator")).build();
    }

    public boolean isSpectating(Player player) {
        return this.spectators.contains(player);
    }

    public Player getTarget(Player player) {
        return this.targetMap.get(player);
    }

    public List<Player> getPlayersWithCertainTarget(Player t) {
        List<Player> players = Lists.newArrayList();
        this.targetMap.forEach((player, target) -> {
            if(t.equals(target)) {
                players.add(player);
            }
        });
        return players;
    }
}
