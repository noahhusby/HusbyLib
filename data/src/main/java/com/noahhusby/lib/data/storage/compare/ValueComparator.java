package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class ValueComparator extends KeyComparator {
    public ValueComparator(String key) {
        super(key);
    }

    @Override
    public CompareResult save(JsonArray array) {
        CompareResult result = save(array, false);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>(result.getComparedOutput());

        Map<JsonElement, JsonObject> keyedArray = new HashMap<>();
        for (JsonElement e : array) {
            JsonObject object = e.getAsJsonObject();
            keyedArray.put(object.get(key), object);
        }

        for (Map.Entry<JsonElement, JsonObject> e : keyedArray.entrySet()) {
            if (compared.containsKey(e.getValue())) {
                continue;
            }
            JsonObject object = e.getValue();
            JsonObject lastObject = lastSave.get(object.get(key));
            JsonObject lastLoadObject = lastLoad.get(object.get(key));
            if (lastObject != null) {
                for (String elementKey : JsonUtils.keySet(object)) {
                    if (lastObject.get(elementKey) == null || !lastObject.get(elementKey).equals(object.get(elementKey)) || lastLoadObject.get(elementKey) == null || !lastLoadObject.get(elementKey).equals(object.get(elementKey))) {
                        compared.put(e.getValue(), ComparatorAction.UPDATE);
                    }
                }
            }
        }

        lastSave = keyedArray;

        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }

    @Override
    public CompareResult load(StorageHandler handler) {
        CompareResult result = load(handler, false);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>(result.getComparedOutput());

        Map<JsonElement, JsonObject> keyedArray = new HashMap<>();
        for (JsonElement e : result.getRawOutput()) {
            JsonObject object = e.getAsJsonObject();
            keyedArray.put(object.get(key), object);
        }

        for (Map.Entry<JsonElement, JsonObject> e : keyedArray.entrySet()) {
            if (compared.containsKey(e.getValue())) {
                continue;
            }
            JsonObject object = e.getValue();
            JsonObject lastObject = lastLoad.get(object.get(key));
            if (lastObject != null) {
                for (String elementKey : JsonUtils.keySet(object)) {
                    if (lastObject.get(elementKey) == null || !lastObject.get(elementKey).equals(object.get(elementKey))) {
                        compared.put(e.getValue(), ComparatorAction.UPDATE);
                    }
                }
            }
        }

        lastLoad = keyedArray;

        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }
}
