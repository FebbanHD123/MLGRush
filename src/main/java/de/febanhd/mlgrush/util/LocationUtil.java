package de.febanhd.mlgrush.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.json.JSONObject;

public class LocationUtil {

    public static String locationToString(Location location) {
        JSONObject json = new JSONObject();
        json.put("world", location.getWorld().getName());
        json.put("x", location.getX());
        json.put("y", location.getY());
        json.put("z", location.getZ());
        json.put("yaw", location.getYaw());
        json.put("pitch", location.getPitch());
        return json.toString();
    }

    public static Location locationFromString(String str) {
        JSONObject json = new JSONObject(str);
        World world = getWorldByName(json.getString("world"));
        double x = json.getDouble("x");
        double y = json.getDouble("y");
        double z = json.getDouble("z");
        float pitch = json.getFloat("pitch");
        float yaw = json.getFloat("yaw");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static World getWorldByName(String name) {
        boolean exists = false;
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equals(name)) {
                exists = true;
            }
        }
        if (exists) {
            return Bukkit.getWorld(name);
        } else {
            return Bukkit.createWorld(new WorldCreator(name));
        }
    }

}
