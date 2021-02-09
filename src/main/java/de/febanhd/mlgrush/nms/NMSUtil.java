package de.febanhd.mlgrush.nms;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtil {

    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public static Class<?> getOBC(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getServerVersion() + '.' + name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getConnection(Player player) throws Exception {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    public static Object getChannel(Player player) throws Exception {
        Object playerConnection = getConnection(player);
        Field networkManageField = playerConnection.getClass().getField("networkManager");
        Object networkManager = networkManageField.get(playerConnection);
        Field channelField = networkManager.getClass().getField("channel");
        Object channel = channelField.get(networkManager);
        return channel;
    }

    public static void setBlockData(Block block, byte data) {
        try {
            block.getClass().getMethod("setData", byte.class)
                    .invoke(block, data);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendActionbar(Player player, String message) {
        if (player == null || message == null) return;
        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);
        try {

            //1.10 and up
            if (!nmsVersion.startsWith("v1_9_R") && !nmsVersion.startsWith("v1_8_R")) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                return;
            }

            //1.8.x and 1.9.x
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> ppoc = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
            Object packetPlayOutChat;
            Class<?> chat = Class.forName("net.minecraft.server." + nmsVersion + (nmsVersion.equalsIgnoreCase("v1_8_R1") ? ".ChatSerializer" : ".ChatComponentText"));
            Class<?> chatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");

            Method method = null;
            if (nmsVersion.equalsIgnoreCase("v1_8_R1")) method = chat.getDeclaredMethod("a", String.class);

            Object object = nmsVersion.equalsIgnoreCase("v1_8_R1") ? chatBaseComponent.cast(method.invoke(chat, "{'text': '" + message + "'}")) : chat.getConstructor(new Class[]{String.class}).newInstance(message);
            packetPlayOutChat = ppoc.getConstructor(new Class[]{chatBaseComponent, Byte.TYPE}).newInstance(object, (byte) 2);

            Method handle = craftPlayerClass.getDeclaredMethod("getHandle");
            Object iCraftPlayer = handle.invoke(craftPlayer);
            Field playerConnectionField = iCraftPlayer.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(iCraftPlayer);
            Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", packet);
            sendPacket.invoke(playerConnection, packetPlayOutChat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object getNMSEntity(Entity ent) {
        try {

            return ent.getClass().getDeclaredMethod("getHandle").invoke(ent);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Entity spawnQueueEntity(EntityType entityType, Location location) throws Exception {
        org.bukkit.entity.Entity bukkitEntity = location.getWorld().spawnEntity(location, entityType);
        bukkitEntity.setCustomNameVisible(true);
        bukkitEntity.setCustomName(LobbyHandler.queueEntityName);
        Object entity = getNMSEntity(bukkitEntity);
        String serverVersion = Bukkit.getBukkitVersion();
        if(MLGRush.getInstance().isLegacy()) {
            if (serverVersion.contains("1.8")) {
                net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity)bukkitEntity).getHandle();
                NBTTagCompound nbtTagCompound = nmsEntity.getNBTTag();
                if(nbtTagCompound == null) nbtTagCompound = new NBTTagCompound();
                nmsEntity.c(nbtTagCompound);
                nbtTagCompound.setInt("Invulnerable", 1);
                nbtTagCompound.setInt("Silent", 1);
                nbtTagCompound.setInt("NoAI", 1);
                nmsEntity.f(nbtTagCompound);
            } else if(serverVersion.contains("1.12")) {
                entity.getClass().getMethod("setNoGravity", boolean.class).invoke(entity, false);
                entity.getClass().getMethod("setInvulnerable", boolean.class).invoke(entity, true);
                entity.getClass().getMethod("setSilent", boolean.class).invoke(entity, true);
            }
        }else {
            entity.getClass().getMethod("setNoGravity", boolean.class).invoke(entity, true);
            entity.getClass().getMethod("setInvulnerable", boolean.class).invoke(entity, true);
            entity.getClass().getMethod("setSilent", boolean.class).invoke(entity, true);
        }
        return bukkitEntity;
    }

    private static void setBooleanToNBT(Object nbtTagCompound, String key, boolean value) throws Exception {
        nbtTagCompound.getClass().getMethod("setBoolean").invoke(nbtTagCompound, key, value);
    }
}
