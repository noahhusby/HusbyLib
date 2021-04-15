package com.noahhusby.lib.data.storage.compare;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class ValueComparator extends KeyComparator {
    public ValueComparator(String key) {
        super(key);
    }

    @Override
    public CompareResult save(JsonArray array, StorageHandler handler) {
        CompareResult result = super.save(array, handler);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>(result.getComparedOutput());

        Map<JsonElement, JsonObject> keyedSave = getKeyedArray(array);
        Map<JsonElement, JsonObject> keyedLoad = getKeyedArray(handler.load());

        MapDifference<JsonElement, JsonObject> diff = Maps.difference(keyedLoad, keyedSave);
        for(MapDifference.ValueDifference<JsonObject> o : diff.entriesDiffering().values()) {
            compared.put(o.rightValue(), ComparatorAction.UPDATE);
        }

        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }

    @Override
    public CompareResult load(JsonArray array, StorageHandler handler) {
        CompareResult result = super.load(array, handler);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>(result.getComparedOutput());

        Map<JsonElement, JsonObject> keyedSave = getKeyedArray(array);
        Map<JsonElement, JsonObject> keyedLoad = getKeyedArray(handler.load());

        MapDifference<JsonElement, JsonObject> diff = Maps.difference(keyedLoad, keyedSave);
        for(MapDifference.ValueDifference<JsonObject> o : diff.entriesDiffering().values()) {
            compared.put(o.leftValue(), ComparatorAction.UPDATE);
        }

        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }
}
