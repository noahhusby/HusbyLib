package com.noahhusby.lib.data.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
        HikariConfig config = getCredentials().toHikariConfig("jdbc:mysql://");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("verifyServerCertificate", false);
        config.addDataSourceProperty("useSSL", false);
        ds = new HikariDataSource(config);
    }

    private HikariDataSource ds;

    @Override
    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public boolean close() {
        ds.close();
        return true;
    }

    @Override
    public boolean execute(Query query) {
        Connection con = getConnection();
        if (con == null) {
            return false;
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(query.query());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                    con.close();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            con.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    @Override
    public Result select(Select select) {
        Connection con = getConnection();
        if (con == null) {
            return new Result();
        }
        PreparedStatement stmt;
        ResultSet res;
        try {
            stmt = con.prepareStatement(select.query());
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
            con.close();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
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
