package com.noahhusby.lib.data.storage.compare;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class ValueComparator implements Comparator{
    private String key;
    public ValueComparator(String key) {
        this.key = key;
    }

    protected Map<JsonElement, JsonObject> lastSave = null;
    protected Map<JsonElement, JsonObject> lastLoad = null;

    @Override
    public CompareResult save(JsonArray array, StorageHandler handler) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        Map<JsonElement, JsonObject> keyedSave = getKeyedArray(array);
        Map<JsonElement, JsonObject> keyedLoad = getKeyedArray(handler.load());

        if(lastSave == null) {
            // Since the parent list has never been saved before, we will all of the current keys against the stored data and add what is necessary.
            for(Map.Entry<JsonElement, JsonObject> e : keyedSave.entrySet()) {
                if(!keyedLoad.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.ADD);
                }
            }
        } else {
            MapDifference<JsonElement, JsonObject> savedDifference = Maps.difference(lastSave, keyedSave);
            for(Map.Entry<JsonElement, JsonObject> e : savedDifference.entriesOnlyOnLeft().entrySet()) {
                if(keyedLoad.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.REMOVE);
                }
            }

            for(Map.Entry<JsonElement, JsonObject> e : savedDifference.entriesOnlyOnRight().entrySet()) {
                if(!keyedLoad.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.ADD);
                }
            }

            for(Map.Entry<JsonElement, MapDifference.ValueDifference<JsonObject>> e : savedDifference.entriesDiffering().entrySet()) {
                compared.put(e.getValue().rightValue(), ComparatorAction.UPDATE);
            }
        }

        lastSave = keyedSave;
        return new CompareResult(array, compared, key, false);
    }

    @Override
    public CompareResult load(JsonArray array, StorageHandler handler) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        Map<JsonElement, JsonObject> keyedSave = getKeyedArray(array);
        Map<JsonElement, JsonObject> keyedLoad = getKeyedArray(handler.load());

        if(lastLoad == null) {
            // Since the parent list has never been saved before, we will all of the current keys against the stored data and add what is necessary.
            for(Map.Entry<JsonElement, JsonObject> e : keyedLoad.entrySet()) {
                if(!keyedSave.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.ADD);
                }
            }
        } else {
            MapDifference<JsonElement, JsonObject> loadedDifference = Maps.difference(lastLoad, keyedLoad);
            for(Map.Entry<JsonElement, JsonObject> e : loadedDifference.entriesOnlyOnLeft().entrySet()) {
                if(keyedSave.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.REMOVE);
                }
            }

            for(Map.Entry<JsonElement, JsonObject> e : loadedDifference.entriesOnlyOnRight().entrySet()) {
                if(!keyedSave.containsKey(e.getKey())) {
                    compared.put(e.getValue(), ComparatorAction.ADD);
                }
            }

            for(Map.Entry<JsonElement, MapDifference.ValueDifference<JsonObject>> e : loadedDifference.entriesDiffering().entrySet()) {
                compared.put(e.getValue().rightValue(), ComparatorAction.UPDATE);
            }
        }

        lastLoad = keyedLoad;
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
