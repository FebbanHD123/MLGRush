package de.febanhd.mlgrush.nms;

import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface NMSBase {

    Entity spawnQueueEntity(EntityType entityType, Location location);

    void sendActionbar(Player player, String string);

    GameProfile getGameProfile(Player player);

    void setBlockData(Block block, byte data);

    void setUnbreakable(ItemStack stack, ItemMeta meta);
}
