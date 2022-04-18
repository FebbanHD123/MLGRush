package de.febanhd.mlgrush.map.template;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Getter
public class MapTemplate {

    private final Cuboid region;
    private final String name;
    private final World world;
    private final Location[] spawnLocation = new Location[2];
    private final BedObject[] bedObjects = new BedObject[2];
    private final Location deathLocation;
    private final Location maxBuildLocation;
    private final MapCreator mapCreator = new MapCreator(this);


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

    public void requestMap(Player player1, Player player2, Consumer<Map> consumer) {
        this.mapCreator.requestMap(player1, player2, consumer);
    }

    private boolean worldExists(String worldName) {
        for(World world : Bukkit.getWorlds()) {
            if(world.getName().equals(worldName)) return true;
        }
        return false;
    }
}
