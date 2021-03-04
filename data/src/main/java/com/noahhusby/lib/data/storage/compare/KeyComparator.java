package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class KeyComparator implements Comparator {
    protected Map<JsonElement, JsonObject> lastSave = new HashMap<>();
    protected Map<JsonElement, JsonObject> lastLoad = new HashMap<>();

    protected final String key;

    public KeyComparator(String key) {
        this.key = key;
    }

    @Override
    public CompareResult save(JsonArray array) {
        return save(array, true);
    }

    protected CompareResult save(JsonArray array, boolean save) {
        Map<JsonElement, JsonObject> keyedArray = new HashMap<>();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        for (JsonElement e : array) {
            JsonObject object = e.getAsJsonObject();
            keyedArray.put(object.get(key), object);
        }

        // Adds object if the last save did not have the object as well as the last load
        for (Map.Entry<JsonElement, JsonObject> e : keyedArray.entrySet()) {
            if (!lastSave.containsKey(e.getKey()) && !lastLoad.containsKey(e.getKey())) {
                compared.put(e.getValue(), ComparatorAction.ADD);
            }
        }

        // Removes an object if the new data does not contian the key, but the last load does
        for (Map.Entry<JsonElement, JsonObject> e : lastSave.entrySet()) {
            if (!keyedArray.containsKey(e.getKey()) && lastLoad.containsKey(e.getKey())) {
                compared.put(e.getValue(), ComparatorAction.REMOVE);
            }
        }

        if (save) {
            lastSave = keyedArray;
        }

        return new CompareResult(array, compared, key, false);
    }

    @Override
    public CompareResult load(StorageHandler handler) {
        return load(handler, true);
    }

    protected CompareResult load(StorageHandler handler, boolean save) {
        JsonArray array = handler.load();
        Map<JsonElement, JsonObject> keyedArray = new HashMap<>();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        for (JsonElement e : array) {
            JsonObject object = e.getAsJsonObject();
            keyedArray.put(object.get(key), object);
        }

        for (Map.Entry<JsonElement, JsonObject> e : keyedArray.entrySet()) {
            if (!lastLoad.containsKey(e.getKey()) && !lastSave.containsKey(e.getKey())) {
                compared.put(e.getValue(), ComparatorAction.ADD);
            }
        }

        for (Map.Entry<JsonElement, JsonObject> e : lastLoad.entrySet()) {
            if (!keyedArray.containsKey(e.getKey()) && lastSave.containsKey(e.getKey())) {
                compared.put(e.getValue(), ComparatorAction.REMOVE);
            }
        }

        if (save) {
            lastLoad = keyedArray;
        }

        return new CompareResult(array, compared, key, false);
    }
}
