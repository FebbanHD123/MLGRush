package de.febanhd.mlgrush.util;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Actionbar {

    private String text;

    public Actionbar(String text) {
        this.text = text;
    }

    public void send(Player player) {
        PacketPlayOutChat chat = new PacketPlayOutChat(new ChatComponentText(this.text), (byte)2);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(chat);
    }
}
