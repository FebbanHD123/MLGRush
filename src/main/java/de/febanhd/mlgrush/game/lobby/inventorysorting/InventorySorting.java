package de.febanhd.mlgrush.game.lobby.inventorysorting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.util.InventoryUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

@Getter
public class InventorySorting {

    private Player player;
    private ArrayList<ItemElement> elements;

    public InventorySorting(Player player, ArrayList<ItemElement> elements) {
        this.elements = elements;
        this.player = player;
    }

    @Override
    public String toString() {

        ArrayList<String> elementStrings = Lists.newArrayList();
        this.elements.forEach(element -> elementStrings.add(element.toString()));

        JSONObject json = new JSONObject();
        json.put("uuid", player.getUniqueId());
        json.put("elements", elementStrings);

        return json.toString();
    }

    public static InventorySorting fromString(Player player, String str) {
        JSONObject jsonObject = new JSONObject(str);
        JSONArray elementArray = jsonObject.getJSONArray("elements");
        ArrayList<ItemElement> elements = Lists.newArrayList();
        elementArray.forEach(object -> {
            elements.add(ItemElement.fromString(object.toString()));
        });
        return new InventorySorting(player, elements);
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
        if(this.elements.size() == items.size()) {
            this.elements.clear();
            items.forEach((slot, stack) -> elements.add(new ItemElement(stack, slot)));
            callback.accept(true);
            MLGRush.getInstance().getInventorySortingDataHandler().updateSorting(this);
        }else {
            callback.accept(false);
        }
    }

    public static class ItemElement  {

        @Getter
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
