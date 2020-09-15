package de.febanhd.mlgrush;

import de.febanhd.mlgrush.commands.CreateMapTemplateCommand;
import de.febanhd.mlgrush.commands.SetLobbyCommand;
import de.febanhd.mlgrush.commands.SetQueueCommand;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.listener.GameListener;
import de.febanhd.mlgrush.listener.InteractListener;
import de.febanhd.mlgrush.listener.InventoryListener;
import de.febanhd.mlgrush.listener.PlayerConnectionListener;
import de.febanhd.mlgrush.map.MapManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MLGRush extends JavaPlugin {

    @Getter
    private static MLGRush instance;
    public static final String PREFIX = "§8[§cA-MLGRush§8] §r";

    private MapManager mapManager;
    private GameHandler gameHandler;

    @Override
    public void onEnable() {
        instance = this;

        this.mapManager = new MapManager();
        this.gameHandler = new GameHandler();

        this.getCommand("setup").setExecutor(new CreateMapTemplateCommand());
        this.getCommand("setlobby").setExecutor(new SetLobbyCommand());
        this.getCommand("setqueue").setExecutor(new SetQueueCommand());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new PlayerConnectionListener(), this);
        pm.registerEvents(new InteractListener(), this);
        pm.registerEvents(new GameListener(this.gameHandler), this);

        this.startProtectionTask();
    }

    @Override
    public void onDisable() {
        this.mapManager.resetMapWorlds();
        System.out.println("[A-MLGRush] Saving map-templates");
        this.mapManager.getMapTemplateStorage().saveAllTemplates();
        if(this.gameHandler.getLobbyHandler().getQueueEntity() != null) {
            this.gameHandler.getLobbyHandler().getQueueEntity().remove();
        }
    }

    private void startProtectionTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            for(World world : Bukkit.getWorlds()) {
                world.setThundering(false);
                world.setStorm(false);
                world.setTime(10000);
                world.getEntities().forEach(entity -> {
                    if(!(entity instanceof Player) && !entity.equals(this.gameHandler.getLobbyHandler().getQueueEntity()) && entity.getType() != EntityType.DROPPED_ITEM) {
                        entity.remove();
                    }
                });
            }
        }, 0, 5);
    }
}
