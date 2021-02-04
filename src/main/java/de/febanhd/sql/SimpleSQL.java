package de.febanhd.sql;

import lombok.Getter;
import de.febanhd.sql.database.builder.SQLBuilder;
import de.febanhd.sql.database.connection.DatabaseConnector;

public class SimpleSQL {

    @Getter
    private final DatabaseConnector connector;

    /**
     * @param connector a database connector
     */

    public SimpleSQL(DatabaseConnector connector) {
        this.connector = connector;
    }

    /**
     *
     * @param sql The de.febanhd.sql statement
     */

    public SQLBuilder createBuilder(String sql) {
        return new SQLBuilder(sql, this);
    }

    /*
    * close the databaseconnector(connections)
    */

    public void closeConnections() {
        this.connector.close();
    }

}