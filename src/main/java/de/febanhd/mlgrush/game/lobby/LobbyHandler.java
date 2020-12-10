package de.febanhd.mlgrush.game.lobby;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.lobby.spectator.SpectatorHandler;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import de.febanhd.mlgrush.util.ItemBuilder;
import de.febanhd.mlgrush.util.LocationUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@Getter
public class LobbyHandler {

    private GameHandler gameHandler;
    private Location lobbyLocation, queueEntityLocation;
    private Entity queueEntity;
    private SpectatorHandler spectatorHandler;

    public static String queueEntityName = "";

    public LobbyHandler(GameHandler gameHandler) {
        LobbyHandler.queueEntityName = ChatColor.translateAlternateColorCodes('&', MLGRush.getInstance().getConfig().getString("queue_entity_name"));
        this.gameHandler = gameHandler;
        this.spectatorHandler = new SpectatorHandler();
        this.loadLocations();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            try {
                this.spawnQueue(EntityType.valueOf(MLGRush.getInstance().getConfig().getString("queue_entity_type")));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }, 20 * 10, 20 * 20);
    }

    public void loadLocations() {
        File file = new File(MLGRush.getInstance().getDataFolder(), "lobby.json");
        if(file.exists()) {
            try {
                String str = new String(Files.readAllBytes(file.toPath()));
                this.lobbyLocation = LocationUtil.locationFromString(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File queueFile = new File(MLGRush.getInstance().getDataFolder(), "queue.json");
        if(queueFile.exists()) {
            try {
                String str = new String(Files.readAllBytes(queueFile.toPath()));
                this.queueEntityLocation = LocationUtil.locationFromString(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;
        File file = new File(MLGRush.getInstance().getDataFolder(), "lobby.json");
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(LocationUtil.locationToString(location));
            writer.flush();
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setQueueEntityLocation(Location location) {
        this.queueEntityLocation = location;
        File file = new File(MLGRush.getInstance().getDataFolder(), "queue.json");
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(LocationUtil.locationToString(location));
            writer.flush();
            writer.close();
            this.spawnQueue(EntityType.ENDER_CRYSTAL);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void spawnQueue(EntityType entityType) {
        if(this.queueEntityLocation == null) return;
        try {
            for(Entity e : this.queueEntityLocation.getWorld().getNearbyEntities(this.queueEntityLocation, 5, 5, 5)) {
                if (e.getType().equals(entityType)) {
                    e.remove();
                }
            }
        }catch (Exception e) {
        }
        this.queueEntity = MLGRush.getInstance().getNmsBase().spawnQueueEntity(entityType, this.queueEntityLocation);
//        Entity entity = this.queueEntityLocation.getWorld().spawnEntity(this.queueEntityLocation, entityType);
//        entity.setCustomNameVisible(true);
//        entity.setCustomName(LobbyHandler.queueEntityName);
//        this.setNoAIAndSilent(entity);
//
//        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
//            entity.teleport(this.queueEntityLocation);
//        }, 20);
    }

    public void setLobbyItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setDisplayName(MLGRush.getString("items.challanger")).setUnbreakable(true).build());
        player.getInventory().setItem(3, new ItemBuilder(Material.CHEST).setDisplayName(InventorySortingGui.GUI_NAME).build());
        player.getInventory().setItem(5, this.spectatorHandler.getSpectatorItem());
    }
}
