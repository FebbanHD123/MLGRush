package de.febanhd.sql.database.connection;

import lombok.SneakyThrows;
import de.febanhd.sql.database.config.IDatabaseConfig;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLLiteDatabaseConnector implements DatabaseConnector {

    private File file;
    private Connection connection;

    public SQLLiteDatabaseConnector(IDatabaseConfig config) {
        this.file = new File(config.getDirPath() + "/" +  config.getDatabase().toLowerCase() + ".db");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.connect(file.getAbsolutePath());
    }

    public void connect(String url) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + url);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public Connection getConnection() {
        if(this.connection == null || this.connection.isClosed()) {
            this.connect(this.file.getAbsolutePath());
        }
        return this.connection;
    }

    @SneakyThrows
    @Override
    public void close() {
        this.connection.close();
    }

    @Override
    public boolean isCloseConnection() {
        return false;
    }
}
