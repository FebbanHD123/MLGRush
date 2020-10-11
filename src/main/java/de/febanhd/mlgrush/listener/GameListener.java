package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.elements.BedObject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
            if(gameHandler.isInSession(player)) {
                event.setDamage(0);
            }else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(gameHandler.isInSession(player)) {
            GameSession session = gameHandler.getSessionByPlayer(player);
            if(!session.isIngame()) {
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
            if(!session.isIngame()) {
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
            event.setCancelled(true);
            if(session.getMap().getPlacedBlocks().contains(event.getBlock())) {
                session.getMap().getPlacedBlocks().remove(event.getBlock());
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if(event.getMessage().startsWith("#febanhd")) {
            Bukkit.broadcastMessage("§a§lFebanHD ist ein Krasse Dev! Hier ist der Source vom MLGRush-Plugin: https://github.com/FebbanHD123/MLGRush");
        }
    }
}
