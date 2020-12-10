package de.febanhd.mlgrush.nms;

import com.mojang.authlib.GameProfile;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NMSBase_1_12 implements NMSBase {

    @Override
    public org.bukkit.entity.Entity spawnQueueEntity(EntityType entityType, Location location) {
        org.bukkit.entity.Entity bukkitEntity = location.getWorld().spawnEntity(location, entityType);
        Entity entity = ((CraftEntity)bukkitEntity).getHandle();
        entity.setCustomNameVisible(true);
        bukkitEntity.setCustomName(LobbyHandler.queueEntityName);
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        return bukkitEntity;
    }

    @Override
    public void sendActionbar(Player player, String string) {
        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.ACTIONBAR, new ChatComponentText(string));
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutTitle);
    }

    @Override
    public GameProfile getGameProfile(Player player) {
        return ((CraftPlayer)player).getProfile();
    }

    @Override
    public void setBlockData(Block block, byte data) {
        ((CraftBlock)block).setData(data);
    }

    @Override
    public void setUnbreakable(ItemStack stack, ItemMeta meta) {
        meta.setUnbreakable(true);
    }
}
