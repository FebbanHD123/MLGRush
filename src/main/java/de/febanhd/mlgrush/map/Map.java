package de.febanhd.mlgrush.map;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.map.template.MapTemplate;
import de.febanhd.mlgrush.util.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Getter
public class Map {

    private final Cuboid region;
    private final BedObject[] beds;
    private final Location[] spawnLocation;
    private final MapTemplate template;
    private Player player1, player2;
    private final int x;
    private int deletingTaskID;
    private final ArrayList<Block> placedBlocks = Lists.newArrayList();
    private final int deathHeight;
    private final int maxBuildHeight;
    private State state;

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
        this.state = State.FREE;
    }

    public void setPlayer1(Player player) {
        this.player1 = player;
        this.beds[0].setOwner(player);
    }

    public void setPlayer2(Player player) {
        this.player2 = player;
        this.beds[1].setOwner(player);
    }

    public void setIngame(Player player1, Player player2) {
        setPlayer1(player1);
        setPlayer2(player2);
        this.state = State.INGAME;
    }

    public void setFree() {
        this.state = State.FREE;
    }

    public void deleteAsync(Runnable callback) {
        ArrayList<Block> blocks = Lists.newArrayList();
        this.region.getBlocks().forEach(block -> {
            if(block.getType() != Material.AIR)
                blocks.add(block);
        });

        deletingTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            if(blocks.size() <= 0) {
                Bukkit.getScheduler().cancelTask(this.deletingTaskID);
                if(callback != null)
                    callback.run();
            }
            for(int i = 0; i < blocks.size() && i < 7; i++) {
                Block block = blocks.get(i);
                block.setType(Material.AIR);
                blocks.remove(block);
            }
        }, 0, 3);
    }

    public void deleteSync() {
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
        return x > region.getUpperX() || x < region.getLowerX() || z > region.getUpperZ() || z < region.getLowerZ();
    }

    public boolean isSpawnBlock(Location location) {
        Block block = location.getBlock();
        Location centerLoc = location.clone();
        centerLoc.setX(block.getX() + 0.5D);
        centerLoc.setY(block.getY() + 0.5D);
        centerLoc.setZ(block.getZ() + 0.5D);
        double distance = 1.8;
        return centerLoc.distance(this.spawnLocation[0]) < distance ||  centerLoc.distance(this.spawnLocation[1]) < distance;
    }

    public void resetPlacedBlocks() {
        AtomicInteger taskID = new AtomicInteger();
        taskID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            if(this.placedBlocks.size() <= 0) {
                Bukkit.getScheduler().cancelTask(taskID.get());
                return;
            }
            for(int i = 0; i < this.placedBlocks.size() && i < 15; i++) {
                Block block = this.placedBlocks.get(i);
                block.setType(Material.AIR);
                this.placedBlocks.remove(block);
            }
        }, 1, 1));
    }

    public static enum State {

        INGAME, FREE

    }
}
