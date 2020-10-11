package de.febanhd.mlgrush.map.setup;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;

@Getter
public class MapTemplateWorld {

    private World world;
    private final String worldName = "mlgRush-templates";

    public void create() {
        if(Bukkit.getWorld(worldName) != null) {
            this.world = Bukkit.getWorld(worldName);
        }else {
            this.world = Bukkit.createWorld(new WorldCreator(worldName).type(WorldType.FLAT));
            this.world.getBlockAt(0, 4, 7).setType(Material.EMERALD_BLOCK);
            this.world.getBlockAt(0, 4, -7).setType(Material.EMERALD_BLOCK);
        }
    }

    public void teleportPlayer(Player player) {
        player.teleport(new Location(world, 0, 4, 0));
    }
}
