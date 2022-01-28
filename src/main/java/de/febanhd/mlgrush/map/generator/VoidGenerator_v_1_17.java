package de.febanhd.mlgrush.map.generator;

import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Random;

public class VoidGenerator_v_1_17 extends VoidGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid paramBiomeGrid) {
        ChunkData chunkData = this.createChunkData(world);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunkData.getMinHeight(); y < chunkData.getMaxHeight(); y++) {
                    paramBiomeGrid.setBiome(x, y, z, Biome.PLAINS);
                }
            }
        }

        super.generateBedrock(null, random, chunkX, chunkZ, chunkData);
        return chunkData;
    }
}
