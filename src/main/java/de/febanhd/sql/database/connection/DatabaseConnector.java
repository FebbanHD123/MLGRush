package de.febanhd.sql.database.connection;

import java.sql.Connection;

public interface DatabaseConnector {

    Connection getConnection();

    void close();

    boolean isCloseConnection();
}
