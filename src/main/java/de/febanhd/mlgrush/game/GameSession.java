package de.febanhd.mlgrush.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.inventory.MapChoosingGui;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.util.Actionbar;
import de.febanhd.mlgrush.util.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class GameSession {

    private final Player player1, player2;

    private MapTemplate mapTemplate;
    private Map map;
    @Setter
    private boolean selectingWorld, running;
    private int pointsForWin, resseterTaskID, taskID;
    private HashMap<Player, Integer> points;

    public GameSession(Player player1, Player player2, int pointsForWin) {
        this.player1 = player1;
        this.player2 = player2;
        if(player1.equals(player2)) {
            try {
                throw new IllegalArgumentException("Player1 and Player2 can't be the same.");
            }catch (NullPointerException e) {
                e.printStackTrace();
            }
            return;
        }
        this.selectingWorld = true;
        this.pointsForWin = MLGRush.getInstance().getConfig().getInt("points.for_win");
        this.points = Maps.newHashMap();
        this.points.put(player1, 0);
        this.points.put(player2, 0);
        this.running = false;

        this.openInv();
    }

    public boolean isIngame() {
        return !selectingWorld;
    }

    private void openInv() {
        MapChoosingGui mapChoosingGui = new MapChoosingGui();
        mapChoosingGui.open(player1);
        mapChoosingGui.open(player2);
    }

    public void closeInv() {
        player1.closeInventory();
        player2.closeInventory();
    }

    public void setMapTemplate(MapTemplate mapTemplate) {
        this.mapTemplate = mapTemplate;
        this.selectingWorld = false;
        this.startGame();
    }

    public void respawn(Player player, boolean death) {
        Player otherPlayer = this.isPlayer1(player) ? player2 : player1;
        Location location;
        if(otherPlayer.equals(player1)) {
            location = this.map.getSpawnLocation()[1];
        }else {
            location = this.map.getSpawnLocation()[0];
        }
        player.teleport(location);
        if(death) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)Math.round(20D * MLGRush.getInstance().getConfig().getDouble("no_move_time")), 10));
            player.playSound(player.getLocation(), Sound.VILLAGER_HIT, 2, 1);
        }
        this.setItems(player);
    }

    private void setItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemBuilder(Material.STICK).addEnchant(Enchantment.KNOCKBACK, 1).setDisplayName("§cStick").build());
        player.getInventory().setItem(1, new ItemBuilder(Material.SANDSTONE, 64).build());
        player.getInventory().setItem(3, new ItemBuilder(Material.SANDSTONE, 64).build());
        player.getInventory().setItem(2, new ItemBuilder(Material.WOOD_PICKAXE).setUnbreakable(true).addEnchant(Enchantment.DIG_SPEED, 1).build());
    }

    public void startGame() {
        MLGRush.getInstance().getMapManager().joinGame(this.mapTemplate, player1, player2, map -> {
            this.map = map;
            this.running = true;
            this.setItems(player1);
            this.setItems(player2);
            PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 30, 100, false);
            player1.addPotionEffect(effect);
            player2.addPotionEffect(effect);
            Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
                this.respawn(player1, false);
                this.respawn(player2, false);
            }, 20);
            this.startIngameTask();
        });
    }

    public void cancelMapChoosing() {
        this.selectingWorld = false;
        player1.sendMessage(MLGRush.getMessage("messages.map_selection_cancel"));
        player2.sendMessage(MLGRush.getMessage("messages.map_selection_cancel"));
        player1.closeInventory();
        player2.closeInventory();
        MLGRush.getInstance().getGameHandler().getGameSessions().remove(this);
    }

    public void stopGame(Player winner) {
        this.running = false;
        this.stopIngameTask();

        Location location = MLGRush.getInstance().getGameHandler().getLobbyHandler().getLobbyLocation();

        if(player1 != null) {
            player1.teleport(location);
            MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player1);
        }
        if(player2 != null) {
            player2.teleport(location);
            MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player2);
        }

        if(this.map != null) {
            this.map.delete();
        }
        if(winner != null) {
            Player looser = null;
            try {
                looser = winner.getUniqueId().equals(player1.getUniqueId()) ? player2 : player1;
            }catch (Exception e) {}

            winner.sendMessage(MLGRush.getMessage("messages.round_win"));

            if(looser != null)
                looser.sendMessage(MLGRush.getMessage("messages.round_loose"));
        }else {
            if (player1 != null)
                player1.sendMessage(MLGRush.getMessage("messages.round_cancel_playerquit"));
            if (player2 != null)
                player2.sendMessage(MLGRush.getMessage("messages.round_cancel_playerquit"));
        }
        MLGRush.getInstance().getGameHandler().getGameSessions().remove(this);
    }

    public void addPoint(Player player) {
        this.points.put(player, this.points.get(player) + 1);
        int points = this.getPoints(player);

        this.respawn(player1, false);
        this.respawn(player2, false);
        this.setItems(player1);
        this.setItems(player2);

        Player otherPlayer = player.equals(player1) ? player2 : player1;

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
        otherPlayer.playSound(player.getLocation(), Sound.VILLAGER_DEATH, 2, 1);

        this.resetPlacedBlocks();

        if(points >= this.pointsForWin) {
            this.stopGame(player);
        }
    }

    private void resetPlacedBlocks() {
        CopyOnWriteArrayList<Block> blocks = Lists.newCopyOnWriteArrayList();
        blocks.addAll(this.map.getPlacedBlocks());
        this.map.getPlacedBlocks().clear();
        this.resseterTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            if(blocks.size() <= 0) {
                Bukkit.getScheduler().cancelTask(this.resseterTaskID);
                return;
            }
            for(int i = 0; i < blocks.size() && i < 5; i++) {
                Block block = blocks.get(i);
                block.setType(Material.AIR);
                blocks.remove(block);
            }
        }, 0, 1);
    }

    private void startIngameTask() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            String actionbarString = ChatColor.RED + player1.getDisplayName() + " §7" + this.getPoints(player1) + " §8| §7" + this.getPoints(player2) + " " + ChatColor.BLUE + player2.getDisplayName();
            Actionbar actionbar = new Actionbar(actionbarString);
            actionbar.send(player1);
            actionbar.send(player2);
        }, 0, 5);
    }

    public void stopIngameTask() {
        Bukkit.getScheduler().cancelTask(this.taskID);
    }

    public int getPoints(Player player) {
        return this.points.get(player);
    }

    public boolean isPlayer1(Player player) {
        if(this.player1 == null) return false;
        return player1.equals(player);
    }
}
