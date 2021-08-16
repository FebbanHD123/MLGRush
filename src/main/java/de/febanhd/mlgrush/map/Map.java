package de.febanhd.mlgrush.map;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.function.Consumer;

@Getter
public class Map {

    private Cuboid region;
    private BedObject[] beds;
    private Location[] spawnLocation;
    private MapTemplate template;
    private Player player1, player2;
    private int x, deletingTaskID;
    private ArrayList<Block> placedBlocks = Lists.newArrayList();
    private int deathHeight, maxBuildHeight;

    public Map(int x, Cuboid region, BedObject[] beds, Location[] spawnLocation, MapTemplate template, int deathHeight, int maxBuildHeight) {
        this.x = x;
        this.region = region.clone();
        this.region.getMaximumPoint().setY(255);
        this.region.getMinimumPoint().setY(0);
        this.beds = beds;
        this.spawnLocation = spawnLocation;
        this.template = template;
        this.deathHeight = deathHeight;
        this.maxBuildHeight = maxBuildHeight;

        MLGRush.getInstance().getMapManager().getMaps().add(this);

    }

    public void setPlayer1(Player player) {
        this.player1 = player;
        this.beds[0].setOwner(player);
    }

    public void setPlayer2(Player player) {
        this.player2 = player;
        this.beds[1].setOwner(player);
    }

    public void delete() {
        this.delete(true);
    }

    public void delete(boolean async) {
        if(async) {
            this.deleteAsync(callback -> {
                this.template.getMapLocationState().put(this.x, MapLocationState.NOT_USED);
                MLGRush.getInstance().getMapManager().getMaps().remove(this);
            });
        }else {
            this.deleteSync();
            MLGRush.getInstance().getMapManager().getMaps().remove(this);
            this.template.getMapLocationState().put(this.x, MapLocationState.NOT_USED);
        }
    }

    private void deleteAsync(Consumer<Boolean> callback) {
        ArrayList<Block> blocks = Lists.newArrayList();
        this.region.getBlocks().forEach(block -> {
            if(block.getType() != Material.AIR)
                blocks.add(block);
        });

        deletingTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            if(blocks.size() <= 0) {
                callback.accept(true);
                Bukkit.getScheduler().cancelTask(this.deletingTaskID);
            }
            for(int i = 0; i < blocks.size() && i < 7; i++) {
                Block block = blocks.get(i);
                block.setType(Material.AIR);
                blocks.remove(block);
            }
        }, 0, 3);
    }

    private void deleteSync() {
        ArrayList<Block> blocks = Lists.newArrayList();
        this.region.getBlocks().forEach(block -> {
            if(block.getType() != Material.AIR)
                blocks.add(block);
        });

        for(Block block : blocks) {
            block.setType(Material.AIR);
        }
    }

    public BedObject getBedOfPlayer(Player player) {
        for(BedObject bedObject : this.beds) {
            if(bedObject.getOwner().equals(player)) {
                return bedObject;
            }
        }
        return null;
    }

    public BedObject getBedOfPlayer2(Player player1) {
        for(BedObject bedObject : this.beds) {
            if(!bedObject.getOwner().equals(player1)) {
                return bedObject;
            }
        }
        return null;
    }

    public boolean isInRegion(Location location) {
        double x = location.getX();
        double z = location.getZ();
        if(x > region.getUpperX() || x < region.getLowerX() || z > region.getUpperZ() || z < region.getLowerZ()) return false;
        return true;
    }

    public boolean isSpawnBlock(Location location) {
        Block block = location.getBlock();
        int x = block.getX();
        int z = block.getZ();
        for (int i = 0; i < this.spawnLocation.length; i++) {
            double maxY = this.spawnLocation[i].getBlockY() + 3;
            double minY = this.spawnLocation[i].getBlockY() + 1.5;
            if(this.spawnLocation[i].getBlockX() == x && this.spawnLocation[i].getBlockZ() == z && location.getY() < maxY && location.getY() > minY)
                return true;
        }
        return false;
    }
}
