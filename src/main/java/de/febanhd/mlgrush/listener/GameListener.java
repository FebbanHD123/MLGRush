package de.febanhd.mlgrush.listener;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.GameSession;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import de.febanhd.mlgrush.game.spectator.SpectatorHandler;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Materials;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;

public class GameListener implements Listener {

    private GameHandler gameHandler;
    private boolean noDamage;

    public GameListener(GameHandler gameHandler, boolean noDamage) {
        this.gameHandler = gameHandler;
        this.noDamage = noDamage;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            GameSession session = gameHandler.getSessionByPlayer(player);
            if(gameHandler.isInSession(player) && gameHandler.getSessionByPlayer(player).isIngame()) {
                if (noDamage) {
                    event.setDamage(0);
                } else if (session.isPlayer1Respawning() && session.isPlayer1(player)) {
                    event.setDamage(0);
                    event.setCancelled(true);
                } else if (session.isPlayer2Respawning() && !session.isPlayer1(player)) {
                    event.setDamage(0);
                    event.setCancelled(true);
                }
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
            System.out.println(damager.getName() + " is spec?: " + spectatorHandler.isSpectating(damager));
            if(spectatorHandler.isSpectating(damager) || spectatorHandler.isSpectating(player)) {
                System.out.println("Spectator");
                event.setCancelled(true);
                return;
            }
            GameHandler gameHandler = MLGRush.getInstance().getGameHandler();
            if(gameHandler.isInSession(damager)) {
                if(!gameHandler.getSessionByPlayer(damager).isIngame()) { ;
                    event.setCancelled(true);
                }
                return;
            }
            if(gameHandler.isInSession(player)) {
                damager.sendMessage(MLGRush.getMessage("messages.lobby.already_one_opponent"));
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if(!damager.getItemInHand().getType().equals(Material.DIAMOND_SWORD)) return;
            if(gameHandler.getTarget(damager) == player) {
                damager.sendMessage(MLGRush.getMessage("messages.lobby.already_challanged").replaceAll("%player%", player.getDisplayName()));
                return;
            }
            gameHandler.setTarget(damager, player);
            damager.sendMessage(MLGRush.getMessage("messages.lobby.challenged").replaceAll("%player%", player.getDisplayName()));
            player.sendMessage(MLGRush.getMessage("messages.lobby.challenged_by_player").replaceAll("%player%", damager.getDisplayName()));
        } else if (event.getDamager() instanceof Player) {
            Entity entity = event.getEntity();
            Player player = (Player)event.getDamager();
            if(entity.getCustomName() != null && entity.getCustomName().equals(LobbyHandler.queueEntityName) && !gameHandler.isInSession(player)) {
                event.setCancelled(true);
                MLGRush.getInstance().getGameHandler().toggleQueue((Player)event.getDamager());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
            if(event.isCancelled())
                event.setCancelled(false);
            if(session.getMap().getMaxBuildHeight() <= event.getBlock().getY() ||
                    session.getMap().isInRegion(event.getBlock().getLocation()) ||
                    session.getMap().isSpawnBlock(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }else {
                session.getMap().getPlacedBlocks().add(event.getBlock());
                if(session.isInfiniteBlocks()) {
                    if(player.getItemInHand().getType().equals(Material.SANDSTONE))
                        player.getInventory().getItemInHand().setAmount(64);
                }
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
            if(event.getBlock().getType() == Materials.BED_BLOCK.getMaterial()) {
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
                if ((player.getLocation().getY() <= map.getDeathHeight() && session.isRunning())) {
                    session.respawn(player, true);
                }else if(map.isInRegion(event.getTo())) {
                    session.respawn(player, false);
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
            Player player = event.getPlayer();
            if(player.getOpenInventory() != null) {
                Inventory openInv = player.getOpenInventory().getTopInventory();
                if(openInv != null && player.getOpenInventory().getTitle().equals(InventorySortingGui.GUI_NAME)) {
                    event.setCancelled(true);
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(event.getEntityType().equals(EntityType.DROPPED_ITEM)) {
            event.setCancelled(true);
        }
    }
}
