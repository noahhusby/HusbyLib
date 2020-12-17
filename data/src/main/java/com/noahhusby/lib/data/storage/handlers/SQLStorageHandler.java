package com.noahhusby.lib.data.storage.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.sql.ISQLDatabase;
import com.noahhusby.lib.data.sql.MySQL;
import com.noahhusby.lib.data.sql.actions.*;
import com.noahhusby.lib.data.storage.Storage;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;
import org.json.simple.JSONObject;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SQLStorageHandler implements StorageHandler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private ISQLDatabase database;

    private boolean initialized = false;
    private final String table;
    private final String columns;
    private final String types;
    private int priority = 0;

    private Storage storage;

    public SQLStorageHandler(String table, String columns, String types) {
        this(new MySQL(), table, columns, types);
    }

    public SQLStorageHandler(ISQLDatabase database, String table, String columns, String types) {
        this.database = database;
        this.table = table;
        this.columns = columns;
        this.types = types;
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
        try {
            if(!initialized) return;
            if(result.isCleared())
                database.execute(new Custom(String.format("DELETE FROM %s;", table)));

            for(Map.Entry<JsonObject, ComparatorAction> r : result.getComparedOutput().entrySet()) {
                JsonObject object = r.getKey();
                if(r.getValue() == ComparatorAction.REMOVE) {
                    database.execute(new Custom(
                            String.format("DELETE FROM %s WHERE %s='%s';", table, result.getKey(), object.get(result.getKey()).getAsString())));
                }

                if(r.getValue() == ComparatorAction.ADD) {
                    List<String> keys = new ArrayList<>();
                    List<String> objects = new ArrayList<>();
                    for(Object k : object.keySet()) {
                        String key = (String) k;
                        keys.add(key);
                        if(object.get(key).isJsonObject() || object.get(key).isJsonArray()) {
                            objects.add(gson.toJson(object.get(key)));
                        } else {
                            objects.add(object.get(key).getAsString());
                        }
                    }

                    String[] finalObjects = new String[objects.size()];
                    objects.toArray(finalObjects);

                    database.execute(new Insert(table, String.join(",", keys), finalObjects));
                }

                if(r.getValue() == ComparatorAction.UPDATE) {
                    UpdateValue update = null;
                    for(String key : object.keySet()) {
                        if(key.equals(result.getKey())) continue;
                        if(update == null) {
                            if(object.get(key).isJsonObject() || object.get(key).isJsonArray()) {
                                update = new UpdateValue(key, gson.toJson(object.get(key)));
                            } else {
                                update = new UpdateValue(key, object.get(key).getAsString());
                            }
                            continue;
                        }
                        if(object.get(key).isJsonObject() || object.get(key).isJsonArray()) {
                            update.add(key, gson.toJson(object.get(key)));
                        } else {
                            update.add(key, object.get(key).getAsString());
                        }
                    }

                    database.execute(new Update(table, update, String.format("%s='%s'", result.getKey(),
                            object.get(result.getKey()).getAsString())));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonArray load() {
        if(!initialized) return new JsonArray();
        Result result = database.select(new Select(table, "*", ""));
        JsonArray array = new JsonArray();

        for(Row r : result.getRows())
            array.add(gson.fromJson(gson.toJson(r.getColumns()), JsonObject.class));

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
        return initialized;
    }

    private void onLoop() {
        if(!initialized) {
            if(!getDatabase().isConnected()) {
                getDatabase().connect();
                return;
            }

            try {
                DatabaseMetaData dbm = getDatabase().getConnection().getMetaData();
                database.execute(new Custom("SET SQL_SAFE_UPDATES = 0;"));

                ResultSet tables = dbm.getTables(getDatabase().getCredentials().getDatabase(), null, table, null);
                if(!tables.next()) {
                    String[] columnArray = columns.split(",");
                    String[] typeArray = types.split(",");
                    StringBuilder query = new StringBuilder(columnArray[0] + " " + typeArray[0]);
                    for(int i = 1; i < columnArray.length; i++)
                        query.append(", ").append(columnArray[i]).append(" ").append(typeArray[i]);
                    getDatabase().execute(new Custom(String.format("CREATE TABLE %s (%s);", table, query.toString())));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            initialized = true;
        }
    }
}
