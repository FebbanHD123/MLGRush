package de.febanhd.mlgrush.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.meta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.meta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(Material material, int amount, short subid) {
        this.itemStack = new ItemStack(material, amount, subid);
        this.meta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(int id, int amount) {
        this.itemStack = new ItemStack(id, amount);
        this.meta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(int id, int amount, short subid) {
        this.itemStack = new ItemStack(id, amount, subid);
        this.meta = this.itemStack.getItemMeta();
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack;
    }

    public ItemBuilder setDisplayName(String displayName) {
        meta.setDisplayName(displayName);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flag) {
        meta.addItemFlags(flag);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.spigot().setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder addEnchant(org.bukkit.enchantments.Enchantment bukkitEnchant, int level) {
        meta.addEnchant(bukkitEnchant, level, true);
        return this;
    }

    public ItemBuilder addEnchantments(Enchantment... enchantments) {
        for(Enchantment enchantment : enchantments) {
            meta.addEnchant(enchantment.getEnchantment(), enchantment.getLevel(), true);
        }
        return this;
    }

    public class Enchantment {
        private int level;
        private org.bukkit.enchantments.Enchantment enchantment;

        public Enchantment(org.bukkit.enchantments.Enchantment enchantment, int level) {
            this.level = level;
            this.enchantment = enchantment;
        }

        public int getLevel() {
            return level;
        }

        public org.bukkit.enchantments.Enchantment getEnchantment() {
            return enchantment;
        }
    }

}
