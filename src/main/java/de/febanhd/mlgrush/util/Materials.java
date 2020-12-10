package de.febanhd.mlgrush.util;

import de.febanhd.mlgrush.MLGRush;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public enum  Materials {
    BED_BLOCK("BED_BLOCK", "RED_BED"),
    EYE_OF_ENDER("EYE_OF_ENDER", "ENDER_EYE"),
    WOOD_PICKAXE("WOOD_PICKAXE", "WOODEN_PICKAXE"),
    PLAYER_HEAD("SKULL_ITEM:3", "PLAYER_HEAD")
    ;

    private String material_legacy;
    private String material;

    Materials(String material_legacy, String material) {
        this.material_legacy = material_legacy;
        this.material = material;
    }

    public ItemBuilder getStack() {
        String materialName;
        if(MLGRush.getInstance().isLegacy()) {
            materialName = this.material_legacy;
        }else {
            materialName= this.material;
        }
        ItemStack stack;
        boolean hasDamage = materialName.contains(":");
        Material material = Material.valueOf(materialName);
        if(hasDamage) {
            short damage = Short.parseShort(materialName.split(":")[1]);
            stack = new ItemStack(material, 1, damage);
        }else {
            stack = new ItemStack(material);
        }
        return new ItemBuilder(stack);
    }

    public Material getMaterial() {
        String materialName;
        if(MLGRush.getInstance().isLegacy()) {
            materialName = this.material_legacy;
        }else {
            materialName = this.material;
        }
        return Material.valueOf(materialName);
    }
}
