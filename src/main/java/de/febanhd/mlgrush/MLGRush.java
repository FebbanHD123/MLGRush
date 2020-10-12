package de.febanhd.mlgrush;

import de.febanhd.mlgrush.commands.*;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.listener.GameListener;
import de.febanhd.mlgrush.listener.InteractListener;
import de.febanhd.mlgrush.listener.InventoryListener;
import de.febanhd.mlgrush.listener.PlayerConnectionListener;
import de.febanhd.mlgrush.map.MapManager;
import de.febanhd.mlgrush.map.setup.MapTemplateWorld;
import de.febanhd.mlgrush.stats.StatsCach;
import de.febanhd.mlgrush.stats.StatsDataHandler;
import de.febanhd.simpleutils.sql.SimpleSQL;
import de.febanhd.simpleutils.sql.database.config.mc.SpigotDatabaseConfig;
import de.febanhd.simpleutils.sql.database.config.sqllite.SQLLiteDatabaseConfig;
import de.febanhd.simpleutils.sql.database.connection.MySQLDatabaseConnector;
import de.febanhd.simpleutils.sql.database.connection.SQLLiteDatabaseConnector;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class MLGRush extends JavaPlugin {

    @Getter
    private static MLGRush instance;
    public static String PREFIX = "§8[§cAdvancedMLGRush§8] §r";
    @Getter
    private static ExecutorService executorService = Executors.newFixedThreadPool(20);

    private MapManager mapManager;
    private GameHandler gameHandler;
    private MapTemplateWorld mapTemplateWorld;

    private SimpleSQL sqlHandler;

    private StatsDataHandler statsDataHandler;

    @Override
    public void onEnable() {
        instance = this;

        this.loadConfig();
        MLGRush.PREFIX = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"));

        this.mapManager = new MapManager();
        this.gameHandler = new GameHandler();

        this.getCommand("setupmap").setExecutor(new SetupMapCommand());
        this.getCommand("setlobby").setExecutor(new SetLobbyCommand());
        this.getCommand("setqueue").setExecutor(new SetQueueCommand());
        this.getCommand("tptemplate").setExecutor(new TPTemplateCommand());
        this.getCommand("leave").setExecutor(new LeaveCommand());
        this.getCommand("stats").setExecutor(new StatsCommand());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new PlayerConnectionListener(), this);
        pm.registerEvents(new InteractListener(), this);
        pm.registerEvents(new GameListener(this.gameHandler), this);

        this.startProtectionTask();

        this.mapTemplateWorld = new MapTemplateWorld();
        this.mapTemplateWorld.create();

        this.loadSql();
        this.statsDataHandler = new StatsDataHandler(this.sqlHandler);

        Bukkit.getOnlinePlayers().forEach(player -> StatsCach.loadStats(player.getUniqueId()));
    }

    @Override
    public void onDisable() {
        this.mapManager.resetMapWorlds();
        System.out.println("[A-MLGRush] Saving map-templates");
        this.mapManager.getMapTemplateStorage().saveAllTemplates();
        if(this.gameHandler.getLobbyHandler().getQueueEntity() != null) {
            this.gameHandler.getLobbyHandler().getQueueEntity().remove();
        }

        this.sqlHandler.closeConnections();
    }

    private void loadConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
    }

    private void loadSql() {
        String databseType = this.getConfig().getString("database");
        if(databseType.equalsIgnoreCase("sqllite")) {
            File file = new File(this.getDataFolder().getPath() + "/databse");
            if(this.getDataFolder().exists())
                this.getDataFolder().mkdir();
            if(!file.exists())file.mkdir();

            this.sqlHandler = new SimpleSQL(new SQLLiteDatabaseConnector(new SQLLiteDatabaseConfig(file.getAbsolutePath(), "database")));

        }else if(databseType.equalsIgnoreCase("mysql")) {
            this.sqlHandler = new SimpleSQL(new MySQLDatabaseConnector(new SpigotDatabaseConfig(this)));
        }
        
        if(this.sqlHandler.getConnector().getConnection() != null)
            this.sqlHandler.createBuilder("CREATE TABLE IF NOT EXISTS mlg_stats (UUID VARCHAR(100) PRIMARY KEY, kills INT NOT NULL , deaths INT NOT NULL , wins INT NOT NULL , looses INT NOT NULL, beds INT NOT NULL)").updateSync();
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

    public static String getMessage(String key) {
        String message;
        if(!MLGRush.getInstance().getConfig().contains(key))
            message = PREFIX + "§4" + key + " §cwas not found! Füge '§7" + key + "§c' in deiner config.yml hinzu. Alternativ kannst du auch deine Config löschen und sie neu erstellen lassen.";
        else
            message = PREFIX + ChatColor.translateAlternateColorCodes('&', MLGRush.getInstance().getConfig().getString(key));

        return message;
    }
}
