package de.febanhd.mlgrush.game.lobby.inventorysorting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.InventoryUtil;
import de.febanhd.mlgrush.util.Sounds;
import de.febanhd.mlgrush.util.UUIDFetcher;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class InventorySorting {

    private final Player player;
    private final ArrayList<ItemElement> elements;
    private InventorySortingDataHandler dataHandler;

    public InventorySorting(Player player, ArrayList<ItemElement> elements) {
        this.player = player;
        this.dataHandler = MLGRush.getInstance().getInventorySortingDataHandler();
        ArrayList<ItemStack> items = Lists.newArrayList();
        elements.forEach(element -> items.add(element.getStack()));
        this.elements = Lists.newArrayList(elements);
        if(!this.isValidElementList(items)) {
            this.elements.clear();
            this.elements.addAll(dataHandler.getDefaultElements());
        }
    }

    @Override
    public String toString() {

        ArrayList<String> elementStrings = Lists.newArrayList();
        this.elements.forEach(element -> elementStrings.add(element.toString()));

        JSONObject json = new JSONObject();
        json.put("uuid", UUIDFetcher.getUUID(player.getName()));
        json.put("elements", elementStrings);

        return json.toString();
    }

    public static InventorySorting fromString(InventorySortingDataHandler dataHandler, Player player, String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            JSONArray elementArray = jsonObject.getJSONArray("elements");
            ArrayList<ItemElement> elements = Lists.newArrayList();
            elementArray.forEach(object -> {
                ItemElement element = ItemElement.fromString(object.toString());
                if(element.getStack().getType().toString().contains("PICKAXE")) {
                    element.setStack(dataHandler.getPickAxeStack());
                }
                elements.add(element);
            });

            return new InventorySorting(player, elements);
        }catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(MLGRush.PREFIX + "§cBeim Laden deiner Inventarsortierung ist ein Fehler aufgetreten! Sie wurde auf Default gesetzt!");
            InventorySorting sorting = new InventorySorting(player, dataHandler.getDefaultElements());
            MLGRush.getInstance().getInventorySortingDataHandler().updateSorting(sorting);
            return sorting;
        }
    }

    public void setToInventory(Inventory inventory) {
        this.elements.forEach(element -> {
            inventory.setItem(element.getSlot(), element.getStack());
        });
    }

    public void updateItems(Inventory inventory, Consumer<Boolean> callback) {
        HashMap<Integer, ItemStack> items = Maps.newHashMap();
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if(stack != null && stack.getType() != Material.AIR) {
                items.put(i, stack);
            }
        }
        if(this.isValidElementList(Lists.newArrayList(items.values()))) {
            this.elements.clear();
            items.forEach((slot, stack) -> elements.add(new ItemElement(stack, slot)));
            MLGRush.getInstance().getInventorySortingDataHandler().updateSorting(this);
            callback.accept(true);
            player.playSound(player.getLocation(), Sounds.LEVEL_UP.getSound(), 2, 1);
        }else {
            callback.accept(false);
        }
    }

    private boolean isValidElementList(List<ItemStack> stacks) {
        ArrayList<ItemElement> rightElements = this.dataHandler.getDefaultElements();
        if(stacks.size() != rightElements.size()) return false;
        for (int i = 0; i < rightElements.size(); i++) {
            ItemElement rightElement = rightElements.get(i);
            boolean valid = false;
            for (int j = 0; j < stacks.size(); j++) {
                if (rightElement.getStack().equals(stacks.get(j))) {
                    valid = true;
                    rightElements.remove(rightElement);
                    break;
                }
            }
            if(!valid) return false;
        }
        return true;
    }



    public static class ItemElement  {

        @Getter
        @Setter
        private ItemStack stack;
        @Getter
        @Setter
        private int slot;

        public ItemElement(ItemStack stack, int slot) {
            this.stack = stack;
            this.slot = slot;
        }

        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            json.put("slot", this.slot);
            json.put("stack", InventoryUtil.itemStackToBase64String(this.stack));
            return json.toString();
        }

        public static ItemElement fromString(String str) {
            JSONObject json = new JSONObject(str);
            ItemStack stack = InventoryUtil.itemStackFromBase64String(json.getString("stack"));
            return new ItemElement(stack, json.getInt("slot"));
        }
    }

}
