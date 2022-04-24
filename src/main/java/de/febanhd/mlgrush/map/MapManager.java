package de.febanhd.mlgrush.map;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.template.MapTemplate;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Getter
public class MapManager {

    private ArrayList<MapTemplate> templates;
    private final MapTemplateStorage mapTemplateStorage;
    private final WorldManager worldManager;

    public MapManager() {
        this.mapTemplateStorage = new MapTemplateStorage();
        this.worldManager = new WorldManager(this.generateVoidWorld());
        Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
            this.templates = this.mapTemplateStorage.loadAllTemplates();
            MLGRush.getInstance().getLogger().info("Loaded " + this.templates.size() + " Map-Template");
        }, 20);
    }

    public void addMapTemplate(MapTemplate template) {
        this.templates.add(template);
        this.mapTemplateStorage.saveInFile(template, this.mapTemplateStorage.getFileFromTemplate(template));
    }

    public MapTemplate getMapTemplate(String mapName) {
        for(MapTemplate template : this.templates) {
            if(template.getName().equalsIgnoreCase(mapName)) {
                return template;
            }
        }
        return null;
    }

    public World generateVoidWorld() {
        String worldName = "mlgrush_live_" + UUID.randomUUID().toString().substring(0, 3);

        File file = new File(worldName);
        if(file.exists())
            file.delete();

        WorldCreator worldCreator = new WorldCreator(worldName)
                .type(WorldType.FLAT)
                .generator(MLGRush.getInstance().getVoidGeneratorProvider().getGenerator());
        return Bukkit.createWorld(worldCreator);
    }

    public String getWorldName(MapTemplate template) {
        return "mlgrush_world_" + template.getName().toLowerCase();
    }

    public void resetMapWorlds() {
        deleteWorld(this.worldManager.getWorld());
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
}
