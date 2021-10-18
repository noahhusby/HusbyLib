/*
 * MIT License
 *
 * Copyright 2020-2021 noahhusby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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
    public void close() {
        ds.close();
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
