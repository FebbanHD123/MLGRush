package de.febanhd.mlgrush.game.lobby.inventorysorting;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.ItemBuilder;
import de.febanhd.mlgrush.util.Materials;
import de.febanhd.simpleutils.sql.SimpleSQL;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class InventorySortingDataHandler {

    private final SimpleSQL databaseHandler;

    public InventorySortingDataHandler(SimpleSQL databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    public static ArrayList<InventorySorting.ItemElement> getDefaultElements() {
        ArrayList<InventorySorting.ItemElement> list = Lists.newArrayList(Arrays.asList(
                new InventorySorting.ItemElement(new ItemBuilder(Material.STICK).setDisplayName(MLGRush.getString("items.stick")).addEnchant(Enchantment.KNOCKBACK, 1).build(), 0),
                new InventorySorting.ItemElement(new ItemBuilder(Materials.WOOD_PICKAXE.getMaterial()).setUnbreakable(true).build(), 1)));
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
        ResultSet rs = this.databaseHandler.createBuilder("SELECT value FROM mlg_inv WHERE UUID=?").addObjects(player.getUniqueId().toString()).querySync();
        if (rs.next()) {
            String value = rs.getString("value");
            return InventorySorting.fromString(player, value);
        }
        return null;
    }

    public InventorySorting createSorting(Player player) {
        InventorySorting sorting = new InventorySorting(player, InventorySortingDataHandler.getDefaultElements());
        this.databaseHandler.createBuilder("INSERT INTO mlg_inv (UUID, value) VALUES (?,?)").addObjects(player.getUniqueId().toString(), sorting.toString()).updateSync();
        return sorting;
    }

    public void updateSorting(InventorySorting sorting) {
        this.databaseHandler.createBuilder("UPDATE mlg_inv SET `value`=?").addObjects(sorting.toString()).updateAsync();
    }

    @SneakyThrows
    public boolean hasSorting(UUID uuid) {
        ResultSet rs = this.databaseHandler.createBuilder("SELECT * FROM mlg_inv WHERE UUID=?").addObjects(uuid.toString()).querySync();
        return rs.next();
    }
}
