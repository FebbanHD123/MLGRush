package de.febanhd.mlgrush.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class SkullBuilder {

    private static Class<?> skullMetaClass, tileEntityClass, blockPositionClass;
    private static int mcVersion;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        mcVersion = Integer.parseInt(version.replaceAll("[^0-9]", ""));
        try {
            skullMetaClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftMetaSkull");
            tileEntityClass = Class.forName("net.minecraft.server." + version + ".TileEntitySkull");
            if (mcVersion > 174) {
                blockPositionClass = Class.forName("net.minecraft.server." + version + ".BlockPosition");
            } else {
                blockPositionClass = null;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param skinURL The URL to the skin-image (full skin)
     * @return The itemstack (SKULL_ITEM) with the given look (skin-image)
     */
    public static ItemStack getSkull(String skinURL) {
        return getSkull(skinURL, 1);
    }
    public static ItemStack getSkull(GameProfile gameProfile, int amount) {
        ItemStack skull = Materials.PLAYER_HEAD.getStack().build();
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        try {
            Field profileField = skullMetaClass.getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, gameProfile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getSkull(String skinURL, int amount) {
        GameProfile gameProfile = getProfile(skinURL);
        return getSkull(gameProfile, amount);
    }

    public static ItemStack getSkull(GameProfile gameProfile) {
        return getSkull(gameProfile, 1);
    }

    public static ItemStack getSkull(UUID uuid) {
        try {
            return getSkull(GameProfileBuilder.fetch(uuid, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static GameProfile getProfile(String skinURL) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        String base64encoded = Base64.getEncoder()
                .encodeToString(new String("{textures:{SKIN:{url:\"" + skinURL + "\"}}}").getBytes());
        Property property = new Property("textures", base64encoded);
        profile.getProperties().put("textures", property);
        return profile;
    }

    private static Object getBlockPositionFor(int x, int y, int z) {
        Object blockPosition = null;
        try {
            Constructor<?> cons = blockPositionClass.getConstructor(int.class, int.class, int.class);
            blockPosition = cons.newInstance(x, y, z);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return blockPosition;
    }
}

