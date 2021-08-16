package de.febanhd.mlgrush.game.lobby.inventorysorting;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.ItemBuilder;
import de.febanhd.mlgrush.util.Materials;
import de.febanhd.mlgrush.util.UUIDFetcher;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import de.febanhd.sql.SimpleSQL;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

public class InventorySortingDataHandler {

    private final SimpleSQL databaseHandler;
    private final int enchantmentPower;

    public InventorySortingDataHandler(SimpleSQL databaseHandler, int enchantmentPower) {
        this.databaseHandler = databaseHandler;
        this.enchantmentPower = enchantmentPower;
    }

    public ItemStack getPickAxeStack() {
        String pickAxeType = MLGRush.getInstance().getConfig().getString("pickaxetype");
        Material pickAxeMaterial;
        if(pickAxeType == null)
            pickAxeMaterial = Material.IRON_PICKAXE;
        else {
            switch (pickAxeType.toUpperCase()) {
                case "WOOD":
                    pickAxeMaterial = Materials.WOOD_PICKAXE.getMaterial();
                    break;
                case "STONE":
                    pickAxeMaterial = Material.STONE_PICKAXE;
                    break;
                case "DIAMOND":
                    pickAxeMaterial = Material.DIAMOND_PICKAXE;
                    break;
                default:
                    pickAxeMaterial = Material.IRON_PICKAXE;
                    break;
            }
        }
        return new ItemBuilder(pickAxeMaterial).build();
    }

    public ArrayList<InventorySorting.ItemElement> getDefaultElements() {
        ArrayList<InventorySorting.ItemElement> list = Lists.newArrayList(Arrays.asList(
                new InventorySorting.ItemElement(new ItemBuilder(Material.STICK).setDisplayName(MLGRush.getString("items.stick")).addEnchant(Enchantment.KNOCKBACK, enchantmentPower).build(), 0),
                new InventorySorting.ItemElement(this.getPickAxeStack(), 1)));
        int blockAmount = MLGRush.getInstance().getConfig().getInt("blockamount");
        if(blockAmount <= 0) {
            blockAmount = 128;
            MLGRush.getInstance().getConfig().set("blockamount", 128);
            MLGRush.getInstance().saveConfig();
        }
        if(blockAmount > 64) {
            int slot  = 2;
            while(blockAmount > 64) {
                list.add(new InventorySorting.ItemElement(new ItemBuilder(Material.SANDSTONE, 64).build(), slot));
                slot++;
                blockAmount -= 64;
                if(blockAmount < 64) {
                    list.add(new InventorySorting.ItemElement(new ItemBuilder(Material.SANDSTONE, blockAmount).build(), slot));
                    break;
                }
            }
        }else {
            list.add(new InventorySorting.ItemElement(new ItemBuilder(Material.SANDSTONE, blockAmount).build(), 2));
        }
        return list;
    }

    @SneakyThrows
    public InventorySorting getSortingFromDB(Player player) {
        ResultSet rs = this.databaseHandler.createBuilder("SELECT value FROM mlg_inv WHERE UUID=?").addObjects(UUIDFetcher.getUUID(player.getName()).toString()).querySync();
        if (rs.next()) {
            String value = rs.getString("value");
            return InventorySorting.fromString(this, player, value);
        }
        return null;
    }

    public InventorySorting createSorting(Player player) {
        InventorySorting sorting = new InventorySorting(player, this.getDefaultElements());
        this.databaseHandler.createBuilder("INSERT INTO mlg_inv (UUID, value) VALUES (?,?)").addObjects(UUIDFetcher.getUUID(player.getName()).toString(), sorting.toString()).updateSync();
        return sorting;
    }

    public void updateSorting(InventorySorting sorting) {
        this.databaseHandler.createBuilder("UPDATE mlg_inv SET `value`=?").addObjects(sorting.toString()).updateAsync();
    }

    @SneakyThrows
    public boolean hasSorting(Player player) {
        ResultSet rs = this.databaseHandler.createBuilder("SELECT * FROM mlg_inv WHERE UUID=?").addObjects(UUIDFetcher.getUUID(player.getName()).toString()).querySync();
        return rs.next();
    }
}
