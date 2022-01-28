package de.febanhd.mlgrush.map.generator;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class VoidGenerator_v_1_17_1 extends VoidGenerator {

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new BiomeProvider() {
            @NotNull
            @Override
            public Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
                return Biome.PLAINS;
            }

            @NotNull
            @Override
            public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                return Collections.singletonList(Biome.PLAINS);
            }
        };
    }
}
