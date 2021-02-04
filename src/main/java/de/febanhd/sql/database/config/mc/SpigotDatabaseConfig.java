package de.febanhd.sql.database.config.mc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import de.febanhd.sql.database.config.IDatabaseConfig;

import java.io.File;
import java.io.IOException;

public class SpigotDatabaseConfig implements IDatabaseConfig {

    private final FileConfiguration cfg;
    private final JavaPlugin plugin;
    private final File configFile;

    public SpigotDatabaseConfig(JavaPlugin spigotPlugin) {
        this.plugin = spigotPlugin;
        this.cfg = YamlConfiguration.loadConfiguration(this.configFile = new File(plugin.getDataFolder(),"database_config.yml"));
        this.init();
    }
    public void init() {
        this.addDefault("DATABASE", this.plugin.getName());
        this.addDefault("HOST", "localhost");
        this.addDefault("PORT", 3306);
        this.addDefault("USER", "root");
        this.addDefault("PASSWORD", "''");
        this.addDefault("maxPoolSize", 10);
        this.addDefault("cachePrepStmts", true);
        this.addDefault("prepStmtCacheSize", 250);
        this.addDefault("prepStmtCacheSqlLimit", 2048);

        try {
            this.cfg.save(this.configFile);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDefault(String path, Object value) {
        if(!this.cfg.contains(path)) {
            this.cfg.set(path, value);
        }
    }

    @Override
    public String getHost() {
        return this.cfg.getString("HOST");
    }

    @Override
    public String getUser() {
        return this.cfg.getString("USER");
    }

    @Override
    public String getPassword() {
        return this.cfg.getString("PASSWORD");
    }

    @Override
    public int getPort() {
        return this.cfg.getInt("PORT");
    }

    @Override
    public String getDatabase() {
        return this.cfg.getString("DATABASE");
    }

    @Override
    public int getMaxPoolSize() {
        return this.cfg.getInt("maxPoolSize");
    }

    @Override
    public boolean isCachePreparedStatements() {
        return this.cfg.getBoolean("cachePrepStmts");
    }

    @Override
    public int getPreparedStatementCacheSize() {
        return this.cfg.getInt("prepStmtCacheSize");
    }

    @Override
    public int getPreparedStatementCacheSQLLimit() {
        return this.cfg.getInt("prepStmtCacheSqlLimit");
    }

    @Override
    public String getDirPath() {
        return this.plugin.getDataFolder().getAbsolutePath();
    }
}
