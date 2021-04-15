package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class CutComparator implements Comparator {
    @Override
    public CompareResult save(JsonArray array, StorageHandler handler) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        for (JsonElement je : array) {
            compared.put(je.getAsJsonObject(), ComparatorAction.ADD);
        }

        return new CompareResult(array, compared, null, true);
    }

    @Override
    public CompareResult load(JsonArray save, StorageHandler handler) {
        JsonArray array = handler.load();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        for (JsonElement je : array) {
            compared.put(je.getAsJsonObject(), ComparatorAction.ADD);
        }

        return new CompareResult(array, compared, null, true);
    }
}
