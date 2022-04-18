package de.febanhd.mlgrush.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.map.template.MapCreator;
import de.febanhd.mlgrush.map.template.MapTemplate;
import de.febanhd.mlgrush.nms.NMSUtil;
import de.febanhd.mlgrush.util.Cuboid;
import de.febanhd.mlgrush.util.Materials;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Bed;

import java.util.*;
import java.util.function.Consumer;

public class MapPaster {

    private final MapTemplate template;
    private final World world;
    private final int x, y;
    private int taskID;
    @Getter
    private int progressPercent;

    public MapPaster(MapTemplate template, World world, int x) {
        this.template = template;
        this.x = x;
        this.y = 200;
        this.progressPercent = 0;
        this.world  = world;
    }

    public void paste(Consumer<Map> callback) {

        ArrayList<Block> blocks = Lists.newArrayList();
        ArrayList<Block> finalBlocks = Lists.newArrayList();

        template.getRegion().getBlocks().forEach(block -> {
            if(block.getType() != Material.AIR) {
                blocks.add(block);
            }
        });

        List<Integer> zList = Lists.newArrayList();
        List<Integer> xList = Lists.newArrayList();
        List<Integer> yList = Lists.newArrayList();
        blocks.forEach(block -> {
            zList.add(block.getZ());
            xList.add(block.getX());
            yList.add(block.getY());
        });

        int minZ = this.getMinOfList(zList), minX = this.getMinOfList(xList), minY = this.getMinOfList(yList);

        final HashMap<Block, Block> blockMap = Maps.newHashMap();

        blocks.forEach(block -> {
            Block finalBlock = this.getNewLocation(block.getLocation(), minX, minY, minZ).getBlock();
            finalBlocks.add(finalBlock);
            blockMap.put(finalBlock, block);
        });

        Cuboid region = this.calculateRegion(finalBlocks);

        this.pasteBlocksAsync(finalBlocks, blockMap, callback2 -> {
            Location spawnLocation1 = this.getNewLocation(template.getSpawnLocation()[0], minX, minY, minZ);
            Location spawnLocation2 = this.getNewLocation(template.getSpawnLocation()[1], minX, minY, minZ);
            spawnLocation1.setWorld(this.world);
            spawnLocation2.setWorld(this.world);

            BedObject bed1 = template.getBedObjects()[0].clone();
            Location bed1FrontLocation = this.getNewLocation(bed1.getFrontLocation(), minX, minY, minZ);
            Location bed1BackLocation = this.getNewLocation(bed1.getBackLocation(), minX, minY, minZ);
            bed1.setFrontLocation(bed1FrontLocation);
            bed1.setBackLocation(bed1BackLocation);
            this.setBed(bed1BackLocation, bed1FrontLocation);

            BedObject bed2 = template.getBedObjects()[1].clone();
            Location bed2FrontLocation = this.getNewLocation(bed2.getFrontLocation(), minX, minY, minZ);
            Location bed2BackLocation = this.getNewLocation(bed2.getBackLocation(), minX, minY, minZ);
            bed2.setFrontLocation(bed2FrontLocation);
            bed2.setBackLocation(bed2BackLocation);
            this.setBed(bed2BackLocation, bed2FrontLocation);

            Location[] spawnLocations = new Location[] {spawnLocation1, spawnLocation2};
            BedObject[] bedObjects = new BedObject[] {bed1, bed2};

            int deathHeight = this.getNewLocation(this.template.getDeathLocation(), minX, minY, minZ).getBlockY();
            int maxBuildHeight = this.getNewLocation(this.template.getMaxBuildLocation(), minX, minY, minZ).getBlockY();

            Map map = new Map(this.x, region, bedObjects, spawnLocations, this.template, deathHeight, maxBuildHeight);

            callback.accept(map);
        });
    }

    private void setBed(Location bedHeadLocation, Location bedFootLocation) {
        try {
            Block bedHeadBlock = bedHeadLocation.getBlock();
            Block bedFootBlock = bedFootLocation.getBlock();
            BlockFace face = bedFootBlock.getFace(bedHeadBlock);

            BlockState bedFootState = bedFootBlock.getState();
            BlockState bedHeadState = bedHeadBlock.getState();

            if(MLGRush.getInstance().isLegacy()) {
                bedFootState.setType(Materials.BED_BLOCK.getMaterial());
                bedHeadState.setType(Materials.BED_BLOCK.getMaterial());

                Bed bedFootData = new Bed(Materials.BED_BLOCK.getMaterial());
                bedFootData.setHeadOfBed(false);
                bedFootData.setFacingDirection(face);
                bedFootState.setData(bedFootData);

                Bed bedHeadData = new Bed(Materials.BED_BLOCK.getMaterial());
                bedHeadData.setHeadOfBed(true);
                bedHeadData.setFacingDirection(face);
                bedHeadState.setData(bedHeadData);
            }else {
                bedHeadBlock.setType(Material.AIR);
                bedFootBlock.setType(Material.AIR);
                Bukkit.getScheduler().runTaskLater(MLGRush.getInstance(), () -> {
                    bedHeadBlock.setType(Materials.BED_BLOCK.getMaterial());
                    bedFootBlock.setType(Materials.BED_BLOCK.getMaterial());
                    bedHeadBlock.setBlockData(Bukkit.createBlockData(Materials.BED_BLOCK.getMaterial(), (data) -> {
                        ((org.bukkit.block.data.type.Bed) data).setPart(org.bukkit.block.data.type.Bed.Part.HEAD);
                        ((org.bukkit.block.data.type.Bed) data).setFacing(face);
                    }));
                    bedFootBlock.setBlockData(Bukkit.createBlockData(Materials.BED_BLOCK.getMaterial(), (data) -> {
                        ((org.bukkit.block.data.type.Bed) data).setPart(org.bukkit.block.data.type.Bed.Part.FOOT);
                        ((org.bukkit.block.data.type.Bed) data).setFacing(face);
                    }));
                }, 20);
            }
            bedFootState.update(true);
            bedHeadState.update(true);
        }catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void pasteBlocksAsync(final ArrayList<Block> blocks, final HashMap<Block, Block> blockMap, final Consumer<Boolean> callback) {
        final int startSize = blocks.size();

        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
           if(blocks.size() <= 0) {
                callback.accept(true);
                Bukkit.getScheduler().cancelTask(this.taskID);
           }
           int rate = MLGRush.getInstance().getConfig().getInt("map_generation.spawningrate");
           if(rate <= 0) rate = 5;
            for(int i = 0; i < blocks.size() && i < rate; i++) {
                Block block = blocks.get(i);
                Block blockToPlace = blockMap.get(block);

                block.setType(blockToPlace.getType());
                if(MLGRush.getInstance().isLegacy())
                    NMSUtil.setBlockDataLegacy(block, blockToPlace.getData());
                else
                    block.setBlockData(blockToPlace.getBlockData());
                block.setBiome(blockToPlace.getBiome());
                block.getState().update();
                blocks.remove(block);
            }
            try {
                this.progressPercent = 100 - ((blocks.size() * 100) / startSize);
            }catch (ArithmeticException e) {
                e.printStackTrace();
            }
        }, 0, 1);
    }

    private Cuboid calculateRegion(ArrayList<Block> blocks) {
        List<Integer> zList = Lists.newArrayList();
        List<Integer> xList = Lists.newArrayList();
        List<Integer> yList = Lists.newArrayList();
        blocks.forEach(block -> {
            zList.add(block.getZ());
            xList.add(block.getX());
            yList.add(block.getY());
        });

        int minZ = this.getMinOfList(zList), minX = this.getMinOfList(xList), minY = this.getMinOfList(yList);
        int maxZ = this.getMaxOfList(zList), maxX = this.getMaxOfList(xList), maxY = this.getMaxOfList(yList);

        int distance = MapCreator.DISTANCE;
        int x1 = minX - (distance / 3);
        int z1 = minZ - (distance / 3);

        int x2 = maxX + (distance / 3);
        int z2 = maxZ + (distance / 3);

        Location loc1 = new Location(this.world, x1, minY - 5, z1);
        Location loc2 = new Location(this.world, x2, maxY + 5, z2);

        return new Cuboid(loc1, loc2);
    }

    private Location getNewLocation(Location location, int minX, int minY, int minZ) {
        double x = this.x + (location.getX() - minX);
        double y = this.y + location.getY() - minY;
        double z = location.getZ() - minZ;

        return new Location(this.world, x, y, z, location.getYaw(), location.getPitch());
    }

    private int getMinOfList(List<Integer> list) {
        if(list.isEmpty()) return 0;
        int min = list.get(0);

        for(int i : list) {
            if(i < min) min = i;
        }
        return min;
    }

    private int getMaxOfList(List<Integer> list) {
        if(list.isEmpty()) return 0;
        int min = list.get(0);

        for(int i : list) {
            if(i > min) min = i;
        }
        return min;
    }
}
