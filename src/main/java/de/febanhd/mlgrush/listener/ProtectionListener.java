package de.febanhd.mlgrush.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class ProtectionListener implements Listener {

    public ProtectionListener() {
        for (World world : Bukkit.getWorlds()) {
            world.getEntities().forEach(Entity::remove);
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
        }
    }

}
