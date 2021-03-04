package com.noahhusby.lib.data.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.sql.actions.Query;
import com.noahhusby.lib.data.sql.actions.Result;
import com.noahhusby.lib.data.sql.actions.Row;
import com.noahhusby.lib.data.sql.actions.Select;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class MySQL extends SQLDatabase {

    private Gson gson = new Gson();

    public MySQL() {
    }

    public MySQL(Credentials credentials) {
        super(credentials);
    }

    private Connection connection;
    private MysqlDataSource data = new MysqlDataSource();

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean connect() {
        Credentials cred = getCredentials();
        data.setServerName(cred.getHost());
        data.setPort(cred.getPort());
        data.setDatabaseName(cred.getDatabase());
        data.setUser(cred.getUsername());
        data.setPassword(cred.getPassword());

        try {
            data.setAllowMultiQueries(true);
            connection = data.getConnection();
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
            if (data.getConnection().isClosed()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
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
        Statement stmt;
        ResultSet res;
        try {
            stmt = connection.createStatement();
            res = stmt.executeQuery(select.query());
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
            System.gc();

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
