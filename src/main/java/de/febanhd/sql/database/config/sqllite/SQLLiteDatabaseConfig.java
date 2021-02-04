package de.febanhd.sql.database.config.sqllite;

import de.febanhd.sql.database.config.IDatabaseConfig;

public class SQLLiteDatabaseConfig implements IDatabaseConfig {

    private String path;
    private String database;

    public SQLLiteDatabaseConfig(String path, String databaseName) {
        this.path = path;
        this.database = databaseName;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public String getDatabase() {
        return this.database;
    }

    @Override
    public int getMaxPoolSize() {
        return 0;
    }

    @Override
    public boolean isCachePreparedStatements() {
        return false;
    }

    @Override
    public int getPreparedStatementCacheSize() {
        return 0;
    }

    @Override
    public int getPreparedStatementCacheSQLLimit() {
        return 0;
    }

    @Override
    public String getDirPath() {
        return this.path;
    }
}
