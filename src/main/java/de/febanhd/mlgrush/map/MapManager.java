package de.febanhd.mlgrush.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.generator.VoidGenerator_v1_8;
import de.febanhd.mlgrush.nms.NMSUtil;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class MapManager {

    public static int DISTANCE = 150;
    public static final int START_X = 50;

    @Getter
    private ArrayList<MapTemplate> templates;
    @Getter
    private MapTemplateStorage mapTemplateStorage;
    private HashMap<UUID, Integer> tasks = Maps.newHashMap();
    @Getter
    private CopyOnWriteArrayList<Map> maps = Lists.newCopyOnWriteArrayList();

    public MapManager() {
        this.mapTemplateStorage = new MapTemplateStorage();
        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
            this.templates = this.mapTemplateStorage.loadAllTemplates();
            MLGRush.getInstance().getLogger().info("Loaded " + this.templates.size() + " Map-Template");
        }, 20);
    }

    public void joinGame(MapTemplate template, Player player1, Player player2, Consumer<Map> callback) {
        final UUID taskUUID = UUID.randomUUID();
        final MapPaster paster = this.generateMap(template, map -> {
            Bukkit.getScheduler().cancelTask(this.tasks.get(taskUUID));
            if(!player1.isOnline() || !player2.isOnline()) {
                map.delete();
                return;
            }
            map.setPlayer1(player1);
            map.setPlayer2(player2);
            player1.teleport(map.getSpawnLocation()[0]);
            player2.teleport(map.getSpawnLocation()[1]);
            player1.setGameMode(GameMode.SURVIVAL);
            player2.setGameMode(GameMode.SURVIVAL);

            callback.accept(map);
        });
        this.tasks.put(taskUUID, Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            String actionBarString = MLGRush.getString("actionbar.loadmap").replaceAll("%percent%", String.valueOf(paster.getProgressPercent()) + "%");
            if(player1.isOnline())
                NMSUtil.sendActionbar(player1, actionBarString);
            if(player2.isOnline())
                NMSUtil.sendActionbar(player2, actionBarString);

            if(!player1.isOnline() || !player2.isOnline()) {
                this.cancelPasting(player1, player2, taskUUID);
            }
        }, 0, 1));
    }

    private void cancelPasting(Player player1, Player player2, UUID taskID) {
        Bukkit.getScheduler().cancelTask(this.tasks.get(taskID));
        if(player1.isOnline()) {
            player1.sendMessage(MLGRush.getMessage("messages.map_creation.cancel"));
        }else {
            player2.sendMessage(MLGRush.getMessage("messages.map_creation.cancel"));
        }
    }

    public void addMapTemplate(MapTemplate template) {
        this.templates.add(template);
        this.mapTemplateStorage.saveInFile(template, this.mapTemplateStorage.getFileFromTemplate(template));
    }

    public MapPaster generateMap(MapTemplate template, Consumer<Map> map) {
        return template.paste(template.getWorld(), template.getXForPaste(), map);
    }

    public MapTemplate getMapTemplate(String mapName) {
        for(MapTemplate template : this.templates) {
            if(template.getName().equalsIgnoreCase(mapName)) {
                return template;
            }
        }
        return null;
    }

    public World generateVoidWorld(MapTemplate mapTemplate) {
        WorldCreator worldCreator = new WorldCreator(
                this.getWorldName(mapTemplate)
        ).type(WorldType.FLAT)
                .generator(MLGRush.getInstance().getVoidGeneratorProvider().getGenerator());
        return Bukkit.createWorld(worldCreator);
    }

    public String getWorldName(MapTemplate template) {
        return "mlgrush_world_" + template.getName().toLowerCase();
    }

    public void resetMapWorlds() {
        for(MapTemplate mapTemplate : this.templates) {
            this.deleteWorld(mapTemplate.getWorld());
        }
    }

    public void deleteWorld(World world) {
        File file = world.getWorldFolder();
        for(Player player : world.getPlayers()) {
            player.kickPlayer(MLGRush.PREFIX + "");
        }
        Bukkit.unloadWorld(world, false);

        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMapIDByName(String name) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(stream);
            out.writeObject(name);

            String id = Base64.getEncoder().encodeToString(stream.toByteArray());

            out.close();
            stream.close();

            return id;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
