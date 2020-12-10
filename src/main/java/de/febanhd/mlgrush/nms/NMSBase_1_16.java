package de.febanhd.mlgrush.nms;

import com.mojang.authlib.GameProfile;
import de.febanhd.mlgrush.game.lobby.LobbyHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NMSBase_1_16 implements NMSBase {

    @Override
    public Entity spawnQueueEntity(EntityType entityType, Location location) {
        Entity bukkitEntity = location.getWorld().spawnEntity(location, entityType);
        net.minecraft.server.v1_16_R1.Entity entity = ((org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity)bukkitEntity).getHandle();
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setCustomNameVisible(true);
        bukkitEntity.setCustomName(LobbyHandler.queueEntityName);
        return bukkitEntity;
    }

    @Override
    public void sendActionbar(Player player, String string) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(string));
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
