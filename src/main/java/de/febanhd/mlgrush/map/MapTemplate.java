package de.febanhd.mlgrush.map;

import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class MapTemplate {

    private Cuboid region;
    private HashMap<Integer, MapLocationState> mapLocationState = Maps.newHashMap();
    private int lastMapX;
    private String name;
    private World world;
    private final Location[] spawnLocation = new Location[2];
    private final BedObject[] bedObjects = new BedObject[2];
    private Location deathLocation, maxBuildLocation;

    public MapTemplate(String name, Cuboid region, Location spawnLocation1, Location spawnLocation2, BedObject bed1, BedObject bed2, Location deathLocation, Location maxBuildLocation) {
        this.region = region;
        this.name = name;

        this.spawnLocation[0] = spawnLocation1;
        this.spawnLocation[1] = spawnLocation2;
        this.bedObjects[0] = bed1;
        this.bedObjects[1] = bed2;

        this.deathLocation = deathLocation;
        this.maxBuildLocation = maxBuildLocation;

        String worldName = MLGRush.getInstance().getMapManager().getWorldName(this);
        if(this.worldExists(worldName)) {
            MLGRush.getInstance().getMapManager().deleteWorld(Bukkit.getWorld(worldName));
        }
        this.world = MLGRush.getInstance().getMapManager().generateVoidWorld(this);
    }

    public MapPaster paste(World world, int x, Consumer<Map> map) {

        this.mapLocationState.put(x, MapLocationState.IN_USE);
        if(this.lastMapX < x) {
            this.lastMapX = x;
        }

        MapPaster paster = new MapPaster(this, world, x);
        paster.paste(map);
        return paster;
    }

    public int getXForPaste() {
        if(this.mapLocationState.size() <= 0) {
            return MapManager.START_X;
        }
         for(java.util.Map.Entry<Integer, MapLocationState> entry : this.mapLocationState.entrySet()) {
            if(entry.getValue() == MapLocationState.NOT_USED) {
                return entry.getKey();
            }
        }
        return this.lastMapX + MapManager.DISTANCE;
    }

    private boolean worldExists(String worldName) {
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equals(worldName)) return true;
        }
        return false;
    }
}
