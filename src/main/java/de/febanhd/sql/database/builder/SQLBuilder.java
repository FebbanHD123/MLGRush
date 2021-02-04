package de.febanhd.sql.database.builder;

import com.google.common.collect.Lists;
import de.febanhd.sql.SimpleSQL;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SQLBuilder {

    private final static ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static RowSetFactory factory = null;

    static {
        try {
            SQLBuilder.factory = RowSetProvider.newFactory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Object> parameters;
    private Connection connection;
    private boolean closeConnection;
    private SimpleSQL databaseHandler;

    private final String SQL;

    public SQLBuilder(final String sql, SimpleSQL handler) {
        this.SQL = sql;
        this.databaseHandler = handler;
        this.parameters = Lists.newArrayList();
        this.connection = this.databaseHandler.getConnector().getConnection();
        this.closeConnection = this.databaseHandler.getConnector().isCloseConnection();
    }

    public SQLBuilder addObjects(Object... objects) {
        this.parameters.addAll(Arrays.asList(objects));
        return this;
    }

    public void updateSync() {
        this.execute(false);
    }

    public CachedRowSet querySync() {
        return this.execute(true);
    }

    public void updateAsync() {
        this.executeAsync(null, false);
    }

    public void updateAsync(Runnable callback) {
        this.executeAsync(new Consumer<CachedRowSet>() {
            @Override
            public void accept(CachedRowSet cachedRowSet) {
                if(callback != null) callback.run();
            }
        }, false);
    }

    public void queryAsync(Consumer<CachedRowSet> callback) {
        this.executeAsync(callback, true);
    }

    private void executeAsync(final Consumer<CachedRowSet> callback, final boolean query) {
        SQLBuilder.THREAD_POOL.execute(() -> {
            CachedRowSet cachedRowSet = this.execute(query);
            if(callback != null) {
                callback.accept(cachedRowSet);
            }
        });
    }

    private CachedRowSet execute(final boolean query) {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        CachedRowSet cachedRowSet = null;
        try {
            statement = this.connection.prepareStatement(this.SQL);
            for(int i = 0; i < this.parameters.size(); i++) {
                statement.setObject(i + 1, this.parameters.get(i));
            }

            if(query) {
                resultSet = statement.executeQuery();
                if(resultSet != null) {
                    cachedRowSet = factory.createCachedRowSet();
                    cachedRowSet.populate(resultSet);
                }
            }else {
                statement.executeUpdate();
            }
        }catch (Exception e) {
            this.handleException(e);
        }finally {
            try {
                this.close(resultSet, statement);
            } catch (SQLException e) {
                System.out.println("Error while closing the SQL-Items:");
                e.printStackTrace();
            }
        }
        return cachedRowSet;
    }

    private void close(ResultSet resultSet, PreparedStatement preparedStatement) throws SQLException {
        if(resultSet != null) {
            resultSet.close();
        }

        if(preparedStatement != null) {
            preparedStatement.close();
        }

        if(this.connection != null && this.closeConnection) {
            connection.close();
        }
    }

    private void handleException(Exception e) {
        ArrayList<String> paramaterStrings = Lists.newArrayList();
        this.parameters.forEach(parameter -> paramaterStrings.add(parameter.toString()));

        System.out.println("------------------SQL-Debug------------------");
        System.out.println("Parameters: " + String.join(", ", paramaterStrings));
        e.printStackTrace();
        System.out.println("----------------------------------------------");
    }
}
