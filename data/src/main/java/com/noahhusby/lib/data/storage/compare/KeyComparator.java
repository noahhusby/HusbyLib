package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class KeyComparator implements Comparator {
    protected JsonArray lastUpdate;
    protected final String key;

    public KeyComparator(String key) {
        this.key = key;
    }

    @Override
    public CompareResult save(JsonArray array) {
        return save(array, true);
    }

    protected CompareResult save(JsonArray array, boolean save) {
        if(lastUpdate == null) lastUpdate = new JsonArray();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        JsonArray add = new JsonArray();
        JsonArray remove = new JsonArray();

        for(JsonElement je : array) {
            boolean a = true;
            for(JsonElement ls : lastUpdate) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    a = false;
                    break;
                }
            }

            if(a)
                add.add(je.getAsJsonObject());
        }

        for(JsonElement je : array) {
            boolean r = true;
            for(JsonElement ls : lastUpdate) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    r = false;
                    break;
                }
            }

            if(r)
                remove.add(je.getAsJsonObject());
        }

        for(JsonElement re : remove)
            compared.put(re.getAsJsonObject(), ComparatorAction.REMOVE);

        for(JsonElement ae : add)
            compared.put(ae.getAsJsonObject(), ComparatorAction.ADD);

        if(save)
            lastUpdate = array.deepCopy();
        return new CompareResult(array, compared, key, false);
    }

    @Override
    public CompareResult load(StorageHandler handler) {
        return load(handler, true);
    }

    protected CompareResult load(StorageHandler handler, boolean save) {
        JsonArray array = handler.load();
        if(lastUpdate == null) lastUpdate = new JsonArray();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        JsonArray add = new JsonArray();
        JsonArray remove = new JsonArray();

        for(JsonElement je : array) {
            boolean a = true;
            for(JsonElement ls : lastUpdate) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    a = false;
                    break;
                }
            }

            if(a)
                add.add(je.getAsJsonObject());
        }

        for(JsonElement je : array) {
            boolean r = true;
            for(JsonElement ls : lastUpdate) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    r = false;
                    break;
                }
            }

            if(r)
                remove.add(je.getAsJsonObject());
        }

        for(JsonElement re : remove)
            compared.put(re.getAsJsonObject(), ComparatorAction.REMOVE);

        for(JsonElement ae : add)
            compared.put(ae.getAsJsonObject(), ComparatorAction.ADD);

        if(save)
            lastUpdate = array.deepCopy();
        return new CompareResult(array, compared, key, false);
    }
}
