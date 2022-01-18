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

package com.noahhusby.lib.data.storage.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.sql.MySQL;
import com.noahhusby.lib.data.sql.SQLDatabase;
import com.noahhusby.lib.data.sql.actions.Custom;
import com.noahhusby.lib.data.sql.actions.DisableSafeUpdates;
import com.noahhusby.lib.data.sql.actions.Insert;
import com.noahhusby.lib.data.sql.actions.Result;
import com.noahhusby.lib.data.sql.actions.Row;
import com.noahhusby.lib.data.sql.actions.Select;
import com.noahhusby.lib.data.sql.actions.Update;
import com.noahhusby.lib.data.sql.actions.UpdateValue;
import com.noahhusby.lib.data.sql.structure.Structure;
import com.noahhusby.lib.data.sql.structure.StructureElement;
import com.noahhusby.lib.data.storage.StorageActions;
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SQLStorageHandler<T> extends StorageHandler<T> {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private SQLDatabase database;

    private final String table;
    private final Structure structure;
    private boolean repaired;

    public String filter = "";

    public SQLStorageHandler(String table, Structure structure) {
        this(new MySQL(), table, structure);
    }

    public SQLStorageHandler(SQLDatabase database, String table, Structure structure) {
        this.database = database;
        this.table = table;
        this.structure = structure;
        onLoop();
        executor.scheduleAtFixedRate(this::onLoop, 0, 5, TimeUnit.SECONDS);
    }

    public void setDatabase(SQLDatabase database) {
        this.database = database;
    }

    public SQLDatabase getDatabase() {
        return database;
    }

    /*
    @Override
    public void save(CompareResult result) {
        if (!isAvailable()) {
            return;
        }
        try {
            if (result.isCleared()) {
                database.execute(new Custom(String.format("DELETE FROM %s;", table)));
            }

            for (Map.Entry<JsonObject, ComparatorAction> r : result.getComparedOutput().entrySet()) {
                JsonObject object = r.getKey();
                if (r.getValue() == ComparatorAction.REMOVE) {
                    database.execute(new Custom(
                            String.format("DELETE FROM %s WHERE %s='%s';", table, result.getKey(), object.get(result.getKey()).getAsString())));
                }

                if (r.getValue() == ComparatorAction.ADD) {
                    List<String> keys = new ArrayList<>();
                    List<String> objects = new ArrayList<>();
                    for (String key : JsonUtils.keySet(object)) {
                        if (!structure.getColumnNames().contains(key)) {
                            continue;
                        }
                        keys.add(key);
                        if (object.get(key).isJsonObject() || object.get(key).isJsonArray()) {
                            objects.add(StorageUtil.gson.toJson(object.get(key)));
                        } else {
                            if (object.get(key).isJsonNull()) {
                                objects.add(null);
                            } else {
                                objects.add(object.get(key).getAsString());
                            }
                        }
                    }

                    String[] finalObjects = new String[objects.size()];
                    objects.toArray(finalObjects);

                    database.execute(new Insert(table, String.join(",", keys), finalObjects));
                }

                if (r.getValue() == ComparatorAction.UPDATE) {
                    UpdateValue update = null;
                    for (String key : JsonUtils.keySet(object)) {
                        if (key.equals(result.getKey())) {
                            continue;
                        }
                        if (update == null) {
                            if (object.get(key).isJsonObject() || object.get(key).isJsonArray()) {
                                update = new UpdateValue(key, StorageUtil.gson.toJson(object.get(key)));
                            } else {
                                if (object.get(key).isJsonNull()) {
                                    update = new UpdateValue(key, null);
                                } else {
                                    update = new UpdateValue(key, object.get(key).getAsString());
                                }
                            }
                            continue;
                        }
                        if (object.get(key).isJsonObject() || object.get(key).isJsonArray()) {
                            update.add(key, StorageUtil.gson.toJson(object.get(key)));
                        } else if (object.get(key).isJsonNull()) {
                            update.add(key, "");
                        } else {
                            update.add(key, object.get(key).getAsString());
                        }
                    }

                    String obj = object.get(result.getKey()).isJsonNull() ? null : object.get(result.getKey()).getAsString();

                    database.execute(new Update(table, update, String.format("%s='%s'", result.getKey(), obj)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     */

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    /*
    @Override
    public JsonArray load() {
        if (!isAvailable()) {
            return new JsonArray();
        }
        Result result = database.select(new Select(table, "*", filter));
        JsonArray array = new JsonArray();

        for (Row r : result.getRows()) {
            array.add(StorageUtil.gson.fromJson(StorageUtil.gson.toJson(r.getColumns()), JsonObject.class));
        }

        return array;
    }

     */

    @Override
    public boolean isAvailable() {
        return repaired;
    }

    @Override
    public StorageActions<T> actions() {
        return null;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private void onLoop() {

        if (!repaired) {
            try {

                if (structure.isRepair()) {
                    Connection con = getDatabase().getConnection();
                    if (con == null) {
                        return;
                    }
                    DatabaseMetaData dbm = con.getMetaData();
                    database.execute(new DisableSafeUpdates());

                    ResultSet tables = dbm.getTables(getDatabase().getCredentials().getDatabase(), null, table, null);
                    if (!tables.next()) {
                        StringBuilder query = new StringBuilder(structure.getElements().get(0).getColumn() + " " + structure.getElements().get(0).getType().getQuery());
                        for (int i = 1; i < structure.getElements().size(); i++) {
                            StructureElement s = structure.getElements().get(i);
                            query.append(", ").append(s.getColumn()).append(" ").append(s.getType().getQuery());
                        }
                        getDatabase().execute(new Custom(String.format("CREATE TABLE %s (%s);", table, query)));
                    } else {
                        ResultSetMetaData metaData = con.createStatement().executeQuery(String.format("SELECT * FROM %s", table)).getMetaData();
                        List<String> columnNames = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            columnNames.add(metaData.getColumnName(i));
                        }
                        List<String> structureColumnNames = new ArrayList<>();
                        for (StructureElement se : structure.getElements()) {
                            structureColumnNames.add(se.getColumn());
                            if (!columnNames.contains(se.getColumn())) {
                                database.execute(new Custom(String.format("ALTER TABLE %s ADD COLUMN %s %s;", table, se.getColumn(), se.getType().getQuery())));
                            }
                        }
                        for (String c : columnNames) {
                            if (!structureColumnNames.contains(c)) {
                                database.execute(new Custom(String.format("ALTER TABLE %s DROP COLUMN %s;", table, c)));
                            }
                        }
                    }
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            repaired = true;
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
        executor = null;
        try {
            database.getConnection().close();
            database.close();
        } catch (Exception ignored) {
        }
    }
}
