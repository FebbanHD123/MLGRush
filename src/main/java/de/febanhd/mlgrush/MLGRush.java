package de.febanhd.mlgrush;

import de.febanhd.mlgrush.commands.*;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingCach;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingDataHandler;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import de.febanhd.mlgrush.gui.MapChoosingGui;
import de.febanhd.mlgrush.gui.SpectatorGui;
import de.febanhd.mlgrush.listener.GameListener;
import de.febanhd.mlgrush.listener.InteractListener;
import de.febanhd.mlgrush.listener.InventoryListener;
import de.febanhd.mlgrush.listener.PlayerConnectionListener;
import de.febanhd.mlgrush.map.MapManager;
import de.febanhd.mlgrush.map.setup.MapTemplateWorld;
import de.febanhd.mlgrush.nms.NMSBase;
import de.febanhd.mlgrush.stats.StatsCach;
import de.febanhd.mlgrush.stats.StatsDataHandler;
import de.febanhd.mlgrush.updatechecker.UpdateChecker;
import de.febanhd.mlgrush.util.ApiversionChecker;
import de.febanhd.sql.SimpleSQL;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.febanhd.sql.database.config.mc.SpigotDatabaseConfig;
import de.febanhd.sql.database.config.sqllite.SQLLiteDatabaseConfig;
import de.febanhd.sql.database.connection.MySQLDatabaseConnector;
import de.febanhd.sql.database.connection.SQLLiteDatabaseConnector;

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
    private InventorySortingDataHandler inventorySortingDataHandler;

    private UpdateChecker updateChecker;

    private boolean legacy;

    private NMSBase nmsBase;

    @Override
    public void onEnable() {
        instance = this;

        this.loadConfig();
        MLGRush.PREFIX = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"));

        this.detectVersion();

        this.mapManager = new MapManager();
        this.gameHandler = new GameHandler();

        this.getCommand("mlgrush").setExecutor(new MLGRushCommand());
        this.getCommand("setupmap").setExecutor(new SetupMapCommand());
        this.getCommand("setlobby").setExecutor(new SetLobbyCommand());
        this.getCommand("setqueue").setExecutor(new SetQueueCommand());
        this.getCommand("tptemplate").setExecutor(new TPTemplateCommand());
        this.getCommand("leave").setExecutor(new LeaveCommand());
        this.getCommand("stats").setExecutor(new StatsCommand());
        this.getCommand("sortinv").setExecutor(new SortInvCommand());

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
        this.inventorySortingDataHandler = new InventorySortingDataHandler(this.sqlHandler);

        Bukkit.getOnlinePlayers().forEach(player -> {
            StatsCach.loadStats(player);
            InventorySortingCach.loadSorting(player);
        });

        boolean useBStats = true;
        if(this.getConfig().contains("bstats")) {
            try {
                useBStats = this.getConfig().getBoolean("bstats");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(useBStats) {
            Metrics metrics = new Metrics(this, 9112);
            metrics.addCustomChart(new Metrics.SimplePie("pluginVersion", () -> getDescription().getVersion()));
        }

        this.updateChecker = new UpdateChecker(this, MLGRush.getExecutorService(), 84672);
        this.updateChecker.getVersion(version -> {
           if(!version.equals(this.getDescription().getVersion())) {
               this.getLogger().info("There is a new version of AdvancedMLGRush (" + version + "). Please update your current version to avoid bugs.");
           }
        });
    }

    @Override
    public void onDisable() {
        this.mapManager.resetMapWorlds();
        System.out.println("[A-MLGRush] Saving map-templates");
        this.mapManager.getMapTemplateStorage().saveAllTemplates();
        if(this.gameHandler.getLobbyHandler().getQueueEntity() != null) {
            this.gameHandler.getLobbyHandler().getQueueEntity().remove();
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType())));
        this.sqlHandler.closeConnections();
    }

    private void detectVersion() {
        String version = ApiversionChecker.getVersion();
        this.legacy = version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12");
        this.nmsBase = ApiversionChecker.getNMSBase();
        if(this.nmsBase == null) {
            Bukkit.getConsoleSender().sendMessage(MLGRush.PREFIX + "§4Your server version is not supported! Are you sure you have the latest server file of your version? And that your version is supported at all?");
            Bukkit.getPluginManager().disablePlugin(this);
        }else {
            Bukkit.getConsoleSender().sendMessage(MLGRush.PREFIX + "§aDetected server version: " + version);
        }
    }

    private void loadConfig() {
        this.getConfig().options().copyDefaults();
        this.saveDefaultConfig();

        MapChoosingGui.GUI_NAME = MLGRush.getString("guiname.mapchoosing");
        InventorySortingGui.GUI_NAME = MLGRush.getString("guiname.inventorysorting");
        SpectatorGui.GUI_NAME = MLGRush.getString("guiname.spectator");
        if(this.getConfig().contains("paste.distance")) {
            MapManager.DISTANCE = this.getConfig().getInt("paste.distance");
        }
    }

    private void loadSql() {
        String databseType = this.getConfig().getString("database");
        if(databseType.equalsIgnoreCase("mysql")) {
            this.sqlHandler = new SimpleSQL(new MySQLDatabaseConnector(new SpigotDatabaseConfig(this)));
        }else {
            File file = new File(this.getDataFolder().getPath() + "/databse");
            if(this.getDataFolder().exists())
                this.getDataFolder().mkdir();
            if(!file.exists())file.mkdir();

            this.sqlHandler = new SimpleSQL(new SQLLiteDatabaseConnector(new SQLLiteDatabaseConfig(file.getAbsolutePath(), "database")));
        }
        
        if(this.sqlHandler.getConnector().getConnection() != null) {
            this.sqlHandler.createBuilder("CREATE TABLE IF NOT EXISTS mlg_stats (UUID VARCHAR(100) PRIMARY KEY, kills INT NOT NULL , deaths INT NOT NULL , wins INT NOT NULL , looses INT NOT NULL, beds INT NOT NULL)").updateSync();
            this.sqlHandler.createBuilder("CREATE TABLE IF NOT EXISTS mlg_inv (UUID VARCHAR(100) PRIMARY KEY, value TEXT NOT NULL)").updateSync();
        }else {
            Bukkit.getConsoleSender().sendMessage(PREFIX + "§cThe plugin was disabled because it could not establish a database connection! (MySQL or SQLite)");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void startProtectionTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
            for(World world : Bukkit.getWorlds()) {
                world.setThundering(false);
                world.setStorm(false);
                world.setTime(10000);
                world.getEntities().forEach(entity -> {
                    if(entity instanceof Monster && entity != this.getGameHandler().getLobbyHandler().getQueueEntity()) {
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

    public static String getString(String key) {
        String message;
        if(!MLGRush.getInstance().getConfig().contains(key))
            message = "§cNot Found";
        else
            message = ChatColor.translateAlternateColorCodes('&', MLGRush.getInstance().getConfig().getString(key));
        return message;
    }
}
