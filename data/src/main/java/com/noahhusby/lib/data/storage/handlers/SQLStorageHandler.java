package com.noahhusby.lib.data.storage.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.sql.ISQLDatabase;
import com.noahhusby.lib.data.sql.MySQL;
import com.noahhusby.lib.data.sql.actions.Custom;
import com.noahhusby.lib.data.sql.actions.Insert;
import com.noahhusby.lib.data.sql.actions.Result;
import com.noahhusby.lib.data.sql.actions.Row;
import com.noahhusby.lib.data.sql.actions.Select;
import com.noahhusby.lib.data.sql.actions.Update;
import com.noahhusby.lib.data.sql.actions.UpdateValue;
import com.noahhusby.lib.data.sql.structure.Structure;
import com.noahhusby.lib.data.sql.structure.StructureElement;
import com.noahhusby.lib.data.storage.Storage;
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SQLStorageHandler implements StorageHandler {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ISQLDatabase database;

    private final String table;
    private final Structure structure;
    private int priority = 0;

    private Storage storage;
    private boolean repaired;

    public String filter = "";

    public SQLStorageHandler(String table, Structure structure) {
        this(new MySQL(), table, structure);
    }

    public SQLStorageHandler(ISQLDatabase database, String table, Structure structure) {
        this.database = database;
        this.table = table;
        this.structure = structure;
        onLoop();
        executor.scheduleAtFixedRate(this::onLoop, 0, 5, TimeUnit.SECONDS);
    }

    public void setDatabase(ISQLDatabase database) {
        this.database = database;
    }

    public ISQLDatabase getDatabase() {
        return database;
    }

    @Override
    public void init(Storage storage) {
        this.storage = storage;
    }

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
                    for (Object k : JsonUtils.keySet(object)) {
                        String key = (String) k;
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
                        } else if(object.get(key).isJsonNull()) {
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

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean isAvailable() {
        return repaired;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private void onLoop() {

        if (!repaired) {
            try {
                if (!getDatabase().isConnected()) {
                    getDatabase().connect();
                }

                if (structure.isRepair()) {
                    DatabaseMetaData dbm = getDatabase().getConnection().getMetaData();
                    database.execute(new Custom("SET SQL_SAFE_UPDATES = 0;"));

                    ResultSet tables = dbm.getTables(getDatabase().getCredentials().getDatabase(), null, table, null);
                    if (!tables.next()) {
                        StringBuilder query = new StringBuilder(structure.getElements().get(0).getColumn() + " " + structure.getElements().get(0).getType().getQuery());
                        for (int i = 1; i < structure.getElements().size(); i++) {
                            StructureElement s = structure.getElements().get(i);
                            query.append(", ").append(s.getColumn()).append(" ").append(s.getType().getQuery());
                        }
                        getDatabase().execute(new Custom(String.format("CREATE TABLE %s (%s);", table, query.toString())));
                    } else {
                        if (!getDatabase().isConnected()) {
                            getDatabase().connect();
                        }
                        ResultSetMetaData metaData = getDatabase().getConnection().createStatement().executeQuery(String.format("SELECT * FROM %s", table)).getMetaData();
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(getDatabase().isConnected()) {
                getDatabase().disconnect();
            }
            repaired = true;
        }
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
        executor = null;
        try {
            database.getConnection().close();
            database.close();
        } catch (Exception ignored) { }
    }
}
