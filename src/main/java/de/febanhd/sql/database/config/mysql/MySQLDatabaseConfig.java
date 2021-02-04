package de.febanhd.sql.database.config.mysql;

import de.febanhd.sql.database.config.IDatabaseConfig;

public class MySQLDatabaseConfig implements IDatabaseConfig {

    private final String host, user, password, databse;
    private final int port, maxPoolSize, preparedStatementCacheSize, preparedStatementCachSqlLimit;

    public MySQLDatabaseConfig(String host, String user, String password, String databse, int port, int maxPoolSize, int preparedStatementCacheSize, int preparedStatementCachSqlLimit) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.databse = databse;
        this.port = port;
        this.maxPoolSize = maxPoolSize;
        this.preparedStatementCacheSize = preparedStatementCacheSize;
        this.preparedStatementCachSqlLimit = preparedStatementCachSqlLimit;
    }

    public MySQLDatabaseConfig(String host, String user, String password, String databse, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.databse = databse;
        this.port = port;
        this.maxPoolSize = 10;
        this.preparedStatementCacheSize = 250;
        this.preparedStatementCachSqlLimit = 2048;
    }

    public String getHost() {
        return this.host;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public int getPort() {
        return this.port;
    }

    public String getDatabase() {
        return this.databse;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public boolean isCachePreparedStatements() {
        return true;
    }

    public int getPreparedStatementCacheSize() {
        return this.preparedStatementCacheSize;
    }

    public int getPreparedStatementCacheSQLLimit() {
        return this.preparedStatementCachSqlLimit;
    }

    @Override
    public String getDirPath() {
        return "";
    }
}
