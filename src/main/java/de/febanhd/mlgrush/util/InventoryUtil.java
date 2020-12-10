package de.febanhd.mlgrush.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class InventoryUtil {

    private static String contentsToString(Inventory inv) {
        List<JSONObject> jsons = Lists.newArrayList();
        for(int i = 0; i < inv.getSize(); i++) {
            if(inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR) {
                JSONObject json = new JSONObject();
                json.put("slot", i);
                json.put("stack", itemStackToBase64String(inv.getItem(i)));
                jsons.add(json);
            }
        }
        return new JSONObject().put("contents", jsons).toString();
    }

    private static Map<Integer, ItemStack> contentsFromString(String string) {
        Map<Integer, ItemStack> contents = Maps.newHashMap();
        JSONArray jsonArray = new JSONObject(string).getJSONArray("contents");
        jsonArray.forEach(object -> {
            JSONObject json = new JSONObject(object.toString());
            int slot = json.getInt("slot");
            ItemStack stack = itemStackFromBase64String(json.getString("stack"));
            contents.put(slot, stack);
        });
        return contents;
    }

    @SneakyThrows
    public static String itemStackToBase64String(ItemStack stack) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream out = new BukkitObjectOutputStream(outputStream);
        out.writeObject(stack);
        String str = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        out.close();
        outputStream.close();
        return str;
    }

    @SneakyThrows
    public static ItemStack itemStackFromBase64String(String base64) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        BukkitObjectInputStream in = new BukkitObjectInputStream(inputStream);
        ItemStack stack = (ItemStack)in.readObject();
        in.close();
        inputStream.close();
        return stack;
    }
}
