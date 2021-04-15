package com.noahhusby.lib.data.storage.compare;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class KeyComparator implements Comparator {
    protected boolean saved = false;
    protected boolean loaded = false;
    protected Map<JsonElement, JsonObject> lastSave = null;
    protected Map<JsonElement, JsonObject> lastLoad = null;

    protected final String key;

    public KeyComparator(String key) {
        this.key = key;
    }

    @Override
    public CompareResult save(JsonArray array, StorageHandler handler) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        // Get keyed arrays from current stored list and current list
        Map<JsonElement, JsonObject> keyedSave = getKeyedArray(array);
        Map<JsonElement, JsonObject> keyedLoad = getKeyedArray(handler.load());

        if(!saved) {
            // Since the parent list has never been saved before, we will all of the current keys against the stored data and add what is necessary.
            for(Map.Entry<JsonElement, JsonObject> e : keyedSave.entrySet()) {
                if(!keyedLoad.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.ADD);
                }
            }
        } else {
            MapDifference<JsonElement, JsonObject> diff = Maps.difference(keyedLoad, keyedSave);
            for(JsonObject o : diff.entriesOnlyOnLeft().values()) {
                compared.put(o, ComparatorAction.REMOVE);
            }
            for(JsonObject o : diff.entriesOnlyOnRight().values()) {
                compared.put(o, ComparatorAction.ADD);
            }
        }

        saved = true;
        return new CompareResult(array, compared, key, false);
    }

    @Override
    public CompareResult load(JsonArray array, StorageHandler handler) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        // Get keyed arrays from current stored list and current list
        Map<JsonElement, JsonObject> keyedSave = getKeyedArray(array);
        Map<JsonElement, JsonObject> keyedLoad = getKeyedArray(handler.load());

        if(!loaded) {
            // Since the data has never been loaded before, we will all of the current keys against the current data and add what is necessary.
            for(Map.Entry<JsonElement, JsonObject> e : keyedLoad.entrySet()) {
                if(!keyedSave.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.ADD);
                }
            }
        } else {
            MapDifference<JsonElement, JsonObject> diff = Maps.difference(keyedLoad, keyedSave);
            for(JsonObject o : diff.entriesOnlyOnLeft().values()) {
                compared.put(o, ComparatorAction.ADD);
            }
            for(JsonObject o : diff.entriesOnlyOnRight().values()) {
                compared.put(o, ComparatorAction.REMOVE);
            }
        }

        loaded = true;

        return new CompareResult(array, compared, key, false);
    }

    protected Map<JsonElement, JsonObject> getKeyedArray(JsonArray array) {
        Map<JsonElement, JsonObject> temp = new HashMap<>();
        for (JsonElement e : array) {
            JsonObject object = e.getAsJsonObject();
            temp.put(object.get(key), object);
        }
        return temp;
    }
}
