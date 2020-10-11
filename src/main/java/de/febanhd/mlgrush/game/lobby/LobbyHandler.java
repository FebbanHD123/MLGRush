package de.febanhd.mlgrush.game.lobby;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
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

    public static String queueEntityName = "";

    public LobbyHandler(GameHandler gameHandler) {
        LobbyHandler.queueEntityName = ChatColor.translateAlternateColorCodes('&', MLGRush.getInstance().getConfig().getString("queue_entity_name"));
        this.gameHandler = gameHandler;
        this.loadLocations();
        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
            this.spawnQueue(EntityType.valueOf(MLGRush.getInstance().getConfig().getString("queue_entity_type")));
        }, 20 * 10);
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
        for(Entity e : this.queueEntityLocation.getWorld().getNearbyEntities(this.queueEntityLocation, 5, 5, 5)) {
            if (!(e instanceof Player)) {
                e.remove();
            }
        }
        Entity entity = this.queueEntityLocation.getWorld().spawnEntity(this.queueEntityLocation, entityType);
        entity.setCustomNameVisible(true);
        entity.setCustomName(LobbyHandler.queueEntityName);
        net.minecraft.server.v1_8_R3.Entity nms = ((CraftEntity) entity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nms.e(tag);
        tag.setInt("NoAI", 1);
        tag.setBoolean("Silent", true);
        nms.f(tag);

        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
            entity.teleport(this.queueEntityLocation);
        }, 20);

        this.queueEntity = entity;
    }

    public void setLobbyItems(Player player) {
        player.getInventory().clear();
    }

}
