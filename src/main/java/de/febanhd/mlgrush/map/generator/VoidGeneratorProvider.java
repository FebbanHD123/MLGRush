package de.febanhd.mlgrush.map.generator;

import de.febanhd.mlgrush.MLGRush;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class VoidGeneratorProvider {

    private final VoidGenerator generator;

    public VoidGeneratorProvider() {
        String version = Bukkit.getServer().getBukkitVersion();
        if(version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12")
                || version.contains("1.13") || version.contains("1.14")) {
            this.generator = new VoidGenerator_v1_8();
        }else if (version.contains("1.15") || version.contains("1.16")) {
            this.generator = new VoidGenerator_v_1_15();
        }else if(version.equals("1.17")) {
            this.generator = new VoidGenerator_v_1_17();
        }else {
            this.generator = new VoidGenerator_v_1_17_1();
        }
        Bukkit.getConsoleSender().sendMessage(MLGRush.PREFIX + "§cVoidGenerator: §a" + this.generator.getClass().getSimpleName());
    }
}
