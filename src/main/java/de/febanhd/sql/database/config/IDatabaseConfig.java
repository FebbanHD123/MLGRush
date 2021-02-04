package de.febanhd.sql.database.config;

public interface IDatabaseConfig {

    String getHost();
    String getUser();
    String getPassword();
    int getPort();
    String getDatabase();
    int getMaxPoolSize();
    boolean isCachePreparedStatements();
    int getPreparedStatementCacheSize();
    int getPreparedStatementCacheSQLLimit();
    String getDirPath();
}
