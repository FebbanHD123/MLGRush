package de.febanhd.mlgrush.map.elements;

import de.febanhd.mlgrush.util.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.JSONObject;

@Getter
@Setter
public class BedObject {

    private Player owner;

    private Location frontLocation, backLocation;

    public BedObject(Location fronLocation, Location backLocation) {
        this.frontLocation = fronLocation;
        this.backLocation = backLocation;
    }

    public BedObject(Player owner, Location fronLocation, Location backLocation) {
        this.frontLocation = fronLocation;
        this.backLocation = backLocation;
        this.owner = owner;
    }

    public boolean isBlockOfBed(Block block) {
        return (block.getLocation().equals(frontLocation.getBlock().getLocation()) || (block.getLocation().equals(backLocation.getBlock().getLocation())));
    }

    public BedObject clone() {
        return new BedObject(this.owner, this.frontLocation.clone(), this.backLocation.clone());
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("front", LocationUtil.locationToString(frontLocation));
        json.put("back", LocationUtil.locationToString(backLocation));
        return json.toString();
    }

    public static BedObject fromString(String str) {

        JSONObject json = new JSONObject(str);
        Location fronLocation = LocationUtil.locationFromString(json.getString("front"));
        Location backLocation = LocationUtil.locationFromString(json.getString("back"));

        return new BedObject(fronLocation, backLocation);
    }

}
