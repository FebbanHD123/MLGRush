package de.febanhd.mlgrush.gui;

import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.util.ItemBuilder;
import de.febanhd.mlgrush.util.SkullBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RoundChoosingGui {

    public static String GUI_NAME = "§eAmount of Rounds";
    private static final ItemStack STACK_5 = new ItemBuilder(SkullBuilder.getSkull("http://textures.minecraft.net/texture/ef4ecf110b0acee4af1da343fb136f1f2c216857dfda6961defdbee7b9528")).setDisplayName("§e5").build(),
                                    STACK_10 = new ItemBuilder(SkullBuilder.getSkull("http://textures.minecraft.net/texture/58ae3e9140a51523bcf222fa84149d1d083fd84762cfd6e5d6cee3343f36c81")).setDisplayName("§e10").build(),
                                    STACK_15 = new ItemBuilder(SkullBuilder.getSkull("http://textures.minecraft.net/texture/f66fa8ca72b2c2c3fc9eea8d443ab66a92e1e7eaa12cc604e572c36effa71")).setDisplayName("§e15").build();

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9 * 3, GUI_NAME);
        inv.setItem(10, STACK_5);
        inv.setItem(13, STACK_10);
        inv.setItem(16, STACK_15);
        inv.setItem(26, new ItemBuilder(Material.BARRIER).setDisplayName(MLGRush.getString("guiname.cancel")).build());
        player.openInventory(inv);
    }
}
