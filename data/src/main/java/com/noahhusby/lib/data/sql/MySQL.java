package com.noahhusby.lib.data.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.sql.actions.Query;
import com.noahhusby.lib.data.sql.actions.Result;
import com.noahhusby.lib.data.sql.actions.Row;
import com.noahhusby.lib.data.sql.actions.Select;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class MySQL extends SQLDatabase {

    private final Gson gson = new Gson();

    public MySQL() {
    }

    public MySQL(Credentials credentials) {
        super(credentials);
    }

    private HikariDataSource ds;
    private Connection connection;

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean connect() {
        HikariConfig config = getCredentials().toHikariConfig("jdbc:mysql://");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        ds = new HikariDataSource(getCredentials().toHikariConfig("jdbc:mysql://"));


        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isConnected() {
        if (connection == null) {
            return false;
        }

        try {
            if (connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean close() {
        try {
            connection.close();
            ds.close();
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute(Query query) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(query.query());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    @Override
    public Result select(Select select) {
        PreparedStatement stmt;
        ResultSet res;
        try {
            stmt = connection.prepareStatement(select.query());
            res = stmt.executeQuery();
            ResultSetMetaData resmeta = res.getMetaData();
            Result result = new Result();

            while (res.next()) {
                Row row = new Row();
                int i = 1;
                boolean bound = true;
                while (bound) {
                    try {
                        boolean addedJson = false;
                        if (res.getObject(i) instanceof String) {
                            if (JsonUtils.isJsonValid(res.getString(i))) {
                                addedJson = true;
                                row.addColumn(resmeta.getColumnName(i), gson.fromJson(res.getString(i), JsonElement.class));
                            }
                        }

                        if (!addedJson) {
                            row.addColumn(resmeta.getColumnName(i), res.getObject(i));
                        }

                    } catch (SQLException e) {
                        bound = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    i++;
                }
                result.addRow(row);
            }

            res.close();
            stmt.close();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            return new Result();
        }
    }

    @Override
    public CompletableFuture<Result> asyncSelect(Select select) {
        CompletableFuture<Result> future = new CompletableFuture<>();
        new Thread(() -> future.complete(this.select(select)));
        return future;
    }
}
