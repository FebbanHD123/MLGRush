package de.febanhd.mlgrush.game.inventorysorting;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;

public class InventorySortingCach {

    private static CopyOnWriteArrayList<InventorySorting> playerSortings = Lists.newCopyOnWriteArrayList();

    public static void loadSorting(Player player) {
        MLGRush.getExecutorService().execute(() -> {
            InventorySortingDataHandler handler = MLGRush.getInstance().getInventorySortingDataHandler();
            if(!handler.hasSorting(player)) {
                playerSortings.add(handler.createSorting(player));
            }else {
                playerSortings.add(handler.getSortingFromDB(player));
            }
        });
    }

    public static InventorySorting getSorting(Player player) {
        for(InventorySorting sorting : InventorySortingCach.playerSortings) {
            if(sorting.getPlayer().equals(player)) {
                return sorting;
            }
        }
        return null;
    }

    public static void remove(Player player) {
        InventorySorting sorting = getSorting(player);
        InventorySortingCach.playerSortings.remove(sorting);
    }
}
