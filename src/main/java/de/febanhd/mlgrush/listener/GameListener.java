package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.game.lobby.spectator.SpectatorHandler;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.elements.BedObject;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class GameListener implements Listener {

    private GameHandler gameHandler;

    public GameListener(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            if(gameHandler.isInSession(player) && gameHandler.getSessionByPlayer(player).isIngame()) {
                event.setDamage(0);
            }else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageEntityByEntity(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            SpectatorHandler spectatorHandler = MLGRush.getInstance().getGameHandler().getLobbyHandler().getSpectatorHandler();
            if(spectatorHandler.isSpectating(damager) || spectatorHandler.isSpectating(player)) {
                event.setCancelled(true);
                return;
            }
            if(!damager.getItemInHand().getType().equals(Material.DIAMOND_SWORD)) return;
            GameHandler gameHandler = MLGRush.getInstance().getGameHandler();
            if(gameHandler.isInSession(damager)) return;
            if(gameHandler.isInSession(player)) {
                damager.sendMessage(MLGRush.getMessage("messages.lobby.already_one_opponent"));
                return;
            }
            event.setCancelled(true);
            if(gameHandler.getTarget(damager) == player) {
                damager.sendMessage(MLGRush.getMessage("messages.lobby.already_challanged").replaceAll("%player%", player.getDisplayName()));
                return;
            }
            gameHandler.setTarget(damager, player);
            damager.sendMessage(MLGRush.getMessage("messages.lobby.challenged").replaceAll("%player%", player.getDisplayName()));
            player.sendMessage(MLGRush.getMessage("messages.lobby.challenged_by_player").replaceAll("%player%", damager.getDisplayName()));
            damager.playSound(damager.getLocation(), Sound.LEVEL_UP, 2, 1);
            player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 2, 1);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(gameHandler.isInSession(player)) {
            GameSession session = gameHandler.getSessionByPlayer(player);
            if(!session.isRunning()) {
                if(player.getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
                return;
            }
            if(session.getMap().getMaxBuildHeight() <= event.getBlock().getY()) {
                event.setCancelled(true);
            }else {
                session.getMap().getPlacedBlocks().add(event.getBlock());
            }
        }else if(player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(gameHandler.isInSession(player)) {
            GameSession session = gameHandler.getSessionByPlayer(player);
            if(!session.isRunning()) {
                if(player.getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
                return;
            }
            Map map = session.getMap();
            if(event.getBlock().getType() == Material.BED_BLOCK) {
                event.setCancelled(true);
                BedObject bedObject = map.getBedOfPlayer2(player);
                if(bedObject.isBlockOfBed(event.getBlock())) {
                    session.addPoint(player);
                }
                return;
            }
            if(session.getMap().getPlacedBlocks().contains(event.getBlock())) {
                session.getMap().getPlacedBlocks().remove(event.getBlock());
                event.getBlock().setType(Material.AIR);
            }else {
                event.setCancelled(true);
            }
        }else if(player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(gameHandler.isInSession(player)) {
            GameSession session = gameHandler.getSessionByPlayer(player);
            if(!session.isIngame()) return;
            Map map = session.getMap();
            if(map != null) {
                if (player.getLocation().getY() <= map.getDeathHeight() && session.isRunning()) {
                    session.respawn(player, true);
                }
            }
        }
    }

        /*
        * Here starts the Protection part :D
        */

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
}
