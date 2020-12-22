package de.febanhd.mlgrush.util;

import de.febanhd.mlgrush.MLGRush;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public enum Sounds {
    LEVEL_UP("LEVEL_UP", "ENTITY_PLAYER_LEVELUP")
    ;

    private String sound_legacy;
    private String sound;

    Sounds(String sound_legacy, String sound) {
        this.sound_legacy = sound_legacy;
        this.sound = sound;
    }

    public Sound getSound() {
        String soundName;
        if(MLGRush.getInstance().isLegacy()) {
            soundName = this.sound_legacy;
        }else {
            soundName = this.sound;
        }
        return Sound.valueOf(soundName);
    }
}
