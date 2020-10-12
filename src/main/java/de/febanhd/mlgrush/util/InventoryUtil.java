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

    public static String inventoryToString(Inventory inventory) {
        ByteArrayOutputStream outStream = null;
        BukkitObjectOutputStream out = null;
        String str = "";
        try {
            outStream = new ByteArrayOutputStream();
            out = new BukkitObjectOutputStream(outStream);

            out.writeObject(inventory.getTitle());
            out.writeInt(inventory.getSize());
            out.writeObject(contentsToString(inventory));

            str = Base64.getEncoder().encodeToString(outStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static Inventory inventoryFromString(String str) {
        ByteArrayInputStream inStream = null;
        BukkitObjectInputStream in = null;
        Inventory inv = null;
        try {
            inStream = new ByteArrayInputStream(Base64.getDecoder().decode(str));
            in = new BukkitObjectInputStream(inStream);

            String title = (String) in.readObject();
            int size = in.readInt();

            inv = Bukkit.createInventory(null, size, title);

            Map<Integer, ItemStack> contents = contentsFromString((String)in.readObject());
            for(Map.Entry<Integer, ItemStack> entry : contents.entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue());
            }
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                inStream.close();
                in.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return inv;
    }

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
