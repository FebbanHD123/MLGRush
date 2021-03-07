package de.febanhd.mlgrush.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySorting;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingCach;
import de.febanhd.mlgrush.game.lobby.spectator.SpectatorHandler;
import de.febanhd.mlgrush.gui.MapChoosingGui;
import de.febanhd.mlgrush.gui.RoundChoosingGui;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.nms.NMSUtil;
import de.febanhd.mlgrush.stats.StatsCach;
import de.febanhd.mlgrush.util.Sounds;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
public class GameSession {

    private final Player player1, player2;
    private boolean player1Respawning;
    private boolean player2Respawning;

    private MapTemplate mapTemplate;
    private Map map;
    @Setter
    private boolean selectingWorld, selectingRounds, running;
    private int pointsForWin, resseterTaskID, taskID;
    private HashMap<Player, Integer> points;
    private final boolean infiniteBlocks;
    private final long startedAt = System.currentTimeMillis();

    private final String id = UUID.randomUUID().toString().split("-")[0];

    public GameSession(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.infiniteBlocks = MLGRush.getInstance().getConfig().getBoolean("infinite.blocks");
        if(player1.equals(player2)) {
            try {
                throw new IllegalArgumentException("Player1 and Player2 can't be the same.");
            }catch (NullPointerException e) {
                e.printStackTrace();
            }
            return;
        }
        this.selectingRounds = true;
        this.selectingWorld = true;
        this.pointsForWin = MLGRush.getInstance().getConfig().getInt("points.for_win");
        this.points = Maps.newHashMap();
        this.points.put(player1, 0);
        this.points.put(player2, 0);
        this.running = false;

        this.openInv();
    }

    public boolean isIngame() {
        return running;
    }

    private void openInv() {
        this.startWaitingTask();
        new MapChoosingGui().open(player1);
        new RoundChoosingGui().open(player2);
        player1.playSound(player1.getLocation(), Sounds.LEVEL_UP.getSound(), 2, 1);
        player2.playSound(player1.getLocation(), Sounds.LEVEL_UP.getSound(), 2, 1);
        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
            if(this.selectingWorld) {
                ArrayList<MapTemplate> templates = MLGRush.getInstance().getMapManager().getTemplates();
                this.setMapTemplate(templates.get(new Random().nextInt(templates.size())));
                player1.closeInventory();
            }
            if(this.selectingRounds) {
                this.setPointsForWin(10);
                player1.closeInventory();
            }
        }, 20 * 19);
    }

    public void closeInv() {
        player1.closeInventory();
        player2.closeInventory();
    }

    public void setMapTemplate(MapTemplate mapTemplate) {
        this.mapTemplate = mapTemplate;
        this.selectingWorld = false;
        if(!selectingRounds)
            this.startGame();
    }

    public void setPointsForWin(int pointsForWin) {
        this.pointsForWin = pointsForWin;
        this.selectingRounds = false;
        if(!selectingWorld)
            this.startGame();
    }

    public void respawn(Player player, boolean death) {
        Player otherPlayer = this.isPlayer1(player) ? player2 : player1;
        player.setFallDistance(0.0F);
        Location location;
        if(otherPlayer.equals(player1)) {
            location = this.map.getSpawnLocation()[1];
        }else {
            location = this.map.getSpawnLocation()[0];
        }

        player.teleport(location);

        if(death) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)Math.round(20D * MLGRush.getInstance().getConfig().getDouble("no_move_time")), 10));
            StatsCach.getStats(player).addDeaths();
            StatsCach.getStats(otherPlayer).addKill();
            otherPlayer.playSound(otherPlayer.getLocation(), Sounds.LEVEL_UP.getSound(), 3, 2);
        }
        this.setItems(player);
        player.setHealth(20.0D);
     }

    private void setItems(Player player) {
        player.getInventory().clear();
        InventorySorting sorting = InventorySortingCach.getSorting(player);
        if(sorting == null)
            MLGRush.getInstance().getInventorySortingDataHandler().createSorting(player);
        sorting.setToInventory(player.getInventory());
    }

    public void startGame() {
        this.stopCurrentTask();
        MLGRush.getInstance().getMapManager().joinGame(this.mapTemplate, player1, player2, map -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(!player.equals(player1) && !player.equals(player2)) {
                    player1.hidePlayer(player);
                    player2.hidePlayer(player);
                }
            });
            this.map = map;
            this.running = true;
            this.setItems(player1);
            this.setItems(player2);
            player1.sendMessage(MLGRush.getMessage("messages.points.info").replaceAll("%points%", this.pointsForWin + ""));
            player2.sendMessage(MLGRush.getMessage("messages.points.info").replaceAll("%points%", this.pointsForWin + ""));
            player1.sendMessage(MLGRush.getMessage("messages.leave.usage"));
            player2.sendMessage(MLGRush.getMessage("messages.leave.usage"));
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

    public void stopGame(Player winner, Player quiter) {
        try {
            this.running = false;
            this.stopCurrentTask();

            Location location = MLGRush.getInstance().getGameHandler().getLobbyHandler().getLobbyLocation();

            if (player1 != null) {
                player1.teleport(location);
                MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player1);
            }
            if (player2 != null) {
                player2.teleport(location);
                MLGRush.getInstance().getGameHandler().getLobbyHandler().setLobbyItems(player2);
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                player1.showPlayer(player);
                player2.showPlayer(player);
            });

            if (this.map != null) {
                this.map.delete();
            }
            if (winner != null) {
                Player looser;

                looser = winner.getUniqueId().equals(player1.getUniqueId()) ? player2 : player1;

                winner.sendMessage(MLGRush.getMessage("messages.round_win"));
                looser.sendMessage(MLGRush.getMessage("messages.round_loose"));

                StatsCach.getStats(winner).addWin();
                StatsCach.getStats(looser).addLoose();
            } else {
                if (player1 != null && player1 != quiter) {
                    player1.sendMessage(MLGRush.getMessage("messages.round_cancel_playerquit"));
                    winner = player1;
                }
                if (player2 != null && player2 != quiter) {
                    player2.sendMessage(MLGRush.getMessage("messages.round_cancel_playerquit"));
                    winner = player2;
                }

                StatsCach.getStats(quiter).addLoose();
                StatsCach.getStats(winner).addWin();
            }
            SpectatorHandler spectatorHandler = MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler();
            spectatorHandler.getPlayersWithCertainTarget(player1).forEach(spectatorHandler::cancelSpectating);
            spectatorHandler.getPlayersWithCertainTarget(player2).forEach(spectatorHandler::cancelSpectating);
        }catch (Exception e) {
            e.printStackTrace();
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

        this.resetPlacedBlocks();

        StatsCach.getStats(player).addBedDestroyed();

        if(points >= this.pointsForWin) {
            this.stopGame(player, player);
        }
    }

    private void resetPlacedBlocks() {
        this.resseterTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            if(this.map.getPlacedBlocks().size() <= 0) {
                Bukkit.getScheduler().cancelTask(this.resseterTaskID);
                return;
            }
            for(int i = 0; i < this.map.getPlacedBlocks().size() && i < 15; i++) {
                Block block = this.map.getPlacedBlocks().get(i);
                block.setType(Material.AIR);
                this.map.getPlacedBlocks().remove(block);
            }
        }, 0, 1);
    }

    private void startIngameTask() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            SpectatorHandler spectatorHandler = MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler();

            Set<Player> spectators = Sets.newHashSet();
            spectators.addAll(spectatorHandler.getSpectatorsOf(player1));
            spectators.addAll(spectatorHandler.getSpectatorsOf(player2));

            String actionbarString = ChatColor.RED + player1.getDisplayName() + " §7" + this.getPoints(player1) + " §8| §7" + this.getPoints(player2) + " " + ChatColor.BLUE + player2.getDisplayName();
            NMSUtil.sendActionbar(player1, actionbarString);
            NMSUtil.sendActionbar(player2, actionbarString);

            spectators.forEach(spectator -> {
                NMSUtil.sendActionbar(spectator, actionbarString);
            });
        }, 0, 20L);
    }

    private void startWaitingTask() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            long seconds = (startedAt + 20000 - System.currentTimeMillis()) / 1000;
            String actionbarStringPlayer1 = MLGRush.getString("messages.lobby.waiting_finish_settings").replaceAll("%player%", player2.getDisplayName()).replaceAll("%seconds%", "" + seconds);
            String actionbarStringPlayer2 = MLGRush.getString("messages.lobby.waiting_finish_settings").replaceAll("%player%", player1.getDisplayName()).replaceAll("%seconds%", "" + seconds);
            NMSUtil.sendActionbar(player1, actionbarStringPlayer1);
            NMSUtil.sendActionbar(player2, actionbarStringPlayer2);
        }, 0, 5);
    }

    public void stopCurrentTask() {
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
