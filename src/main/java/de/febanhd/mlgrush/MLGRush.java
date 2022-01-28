package de.febanhd.mlgrush;

import de.febanhd.mlgrush.commands.*;
import de.febanhd.mlgrush.game.GameHandler;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingCach;
import de.febanhd.mlgrush.game.lobby.inventorysorting.InventorySortingDataHandler;
import de.febanhd.mlgrush.gui.InventorySortingGui;
import de.febanhd.mlgrush.gui.MapChoosingGui;
import de.febanhd.mlgrush.gui.RoundChoosingGui;
import de.febanhd.mlgrush.gui.SpectatorGui;
import de.febanhd.mlgrush.listener.GameListener;
import de.febanhd.mlgrush.listener.InteractListener;
import de.febanhd.mlgrush.listener.InventoryListener;
import de.febanhd.mlgrush.listener.PlayerConnectionListener;
import de.febanhd.mlgrush.map.MapManager;
import de.febanhd.mlgrush.map.generator.VoidGeneratorProvider;
import de.febanhd.mlgrush.map.setup.MapTemplateWorld;
import de.febanhd.mlgrush.placeholder.MLGRushPlaceholderExpansion;
import de.febanhd.mlgrush.stats.StatsCach;
import de.febanhd.mlgrush.stats.StatsDataHandler;
import de.febanhd.mlgrush.updatechecker.UpdateChecker;
import de.febanhd.sql.SimpleSQL;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.febanhd.sql.database.config.mc.SpigotDatabaseConfig;
import de.febanhd.sql.database.config.sqllite.SQLLiteDatabaseConfig;
import de.febanhd.sql.database.connection.MySQLDatabaseConnector;
import de.febanhd.sql.database.connection.SQLLiteDatabaseConnector;

import java.io.File;
import java.util.Objects;
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

    private YamlConfiguration messageConfig;

    private VoidGeneratorProvider voidGeneratorProvider;

    private boolean legacy;

    @Override
    public void onEnable() {
        instance = this;

        this.detectVersion();
        this.voidGeneratorProvider = new VoidGeneratorProvider();
        this.loadConfig();
        this.initPlaceHolderAPI();

        MLGRush.PREFIX = ChatColor.translateAlternateColorCodes('&', getString("prefix"));

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
        Bukkit.getPluginManager().registerEvents(new GameListener(this.gameHandler, this.getConfig().getBoolean("no-damage")), this);

        this.startProtectionTask();

        this.mapTemplateWorld = new MapTemplateWorld();
        this.mapTemplateWorld.create();

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
        this.getLogger().info("Saving map-templates");
        this.mapManager.getMapTemplateStorage().saveAllTemplates();
        if(this.gameHandler.getLobbyHandler().getQueueEntity() != null) {
            this.gameHandler.getLobbyHandler().getQueueEntity().remove();
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType())));
        this.sqlHandler.closeConnections();
    }

    private void detectVersion() {
        String version = Bukkit.getServer().getBukkitVersion();
        this.legacy = version.contains("1.8") || version.contains("1.9") || version.contains("1.10") || version.contains("1.11") || version.contains("1.12");
        if(legacy && !version.contains("1.8") && !version.contains("1.12")) {
            Bukkit.getConsoleSender().sendMessage(MLGRush.PREFIX + "§4Your server version is not supported! Are you sure you have the latest server file of your version? And that your version is supported at all?");
            Bukkit.getPluginManager().disablePlugin(this);
        }else {
            Bukkit.getConsoleSender().sendMessage(MLGRush.PREFIX + "§aDetected server version: " + version);
        }
    }

    private void initPlaceHolderAPI() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            //register placeholders
            Bukkit.getConsoleSender().sendMessage(MLGRush.PREFIX + "§aPlaceholderAPI is running on this server. Register a new extension...");
            new MLGRushPlaceholderExpansion().register();
        }
    }

    private void loadConfig() {
        if(!new File(this.getDataFolder(), "messages_de.yml").exists())
            this.saveResource("messages_de.yml", false);
        if(!new File(this.getDataFolder(), "messages_en.yml").exists())
            this.saveResource("messages_en.yml", false);
        if(!new File(this.getDataFolder(), "messages_zh-cn.yml").exists())
            this.saveResource("messages_zh-cn.yml", false);
        if(!new File(this.getDataFolder(), "messages_zh-TW.yml").exists())
            this.saveResource("messages_zh-TW.yml", false);


        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        String language = getConfig().getString("language", "en");
        if (language.equalsIgnoreCase("de")) {
            this.messageConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "messages_de.yml"));
            this.getLogger().info("Sprache geladen!");
        }else if(language.equalsIgnoreCase("zh-cn")) {
            this.messageConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "messages_zh-cn.yml"));
            this.getLogger().info("加载语言!");
        }else if(language.equalsIgnoreCase("zh-TW")) {
            this.messageConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "messages_zh-TW.yml"));
            this.getLogger().info("加载语言!");
        }else {
            this.messageConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "messages_en.yml"));
            this.getLogger().info("Language loaded!");
        }


        MapChoosingGui.GUI_NAME = MLGRush.getString("guiname.mapchoosing");
        InventorySortingGui.GUI_NAME = MLGRush.getString("guiname.inventorysorting");
        SpectatorGui.GUI_NAME = MLGRush.getString("guiname.spectator");
        RoundChoosingGui.GUI_NAME = MLGRush.getString("guiname.roundchoosing");
        if(this.getConfig().contains("paste.distance")) {
            MapManager.DISTANCE = this.getConfig().getInt("paste.distance");
        }

        this.loadSql();
        this.statsDataHandler = new StatsDataHandler(this.sqlHandler);
        this.inventorySortingDataHandler = new InventorySortingDataHandler(this.sqlHandler, this.getConfig().getInt("knockback-amplifier"));
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
        if(!MLGRush.getInstance().getMessageConfig().contains(key))
            message = PREFIX + "§4" + key + " §cwas not found in messages!";
        else
            message = PREFIX + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(MLGRush.getInstance().getMessageConfig().getString(key)));

        return message;
    }

    public static String getString(String key) {
        String message;
        if(!MLGRush.getInstance().getMessageConfig().contains(key))
            message = "§cNot Found";
        else
            message = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(MLGRush.getInstance().getMessageConfig().getString(key)));
        return message;
    }

}
