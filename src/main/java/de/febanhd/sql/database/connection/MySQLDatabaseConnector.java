package de.febanhd.sql.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import de.febanhd.sql.database.config.IDatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

@Getter
public class MySQLDatabaseConnector extends HikariConfig implements DatabaseConnector {

    private final HikariDataSource dataSource;
    private IDatabaseConfig config;

    public MySQLDatabaseConnector(IDatabaseConfig config) {
        this.config = config;
        this.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase());
        this.setUsername(config.getUser());
        this.setPassword(config.getPassword());
        this.setDriverClassName("com.mysql.jdbc.Driver");
        this.addDataSourceProperty("cachePrepStmts", String.valueOf(config.isCachePreparedStatements()));
        this.addDataSourceProperty("prepStmtCacheSize", String.valueOf(config.getPreparedStatementCacheSize()));
        this.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(config.getPreparedStatementCacheSQLLimit()));
        this.setMaximumPoolSize(config.getMaxPoolSize());

        this.dataSource = new HikariDataSource(this);
    }

    @Override
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        }catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        this.dataSource.close();
    }

    @Override
    public boolean isCloseConnection() {
        return true;
    }
}
