package com.noahhusby.lib.data.storage.compare;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.lang.reflect.Type;
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

        for(Map.Entry<JsonElement, JsonObject> e : keyedArray.entrySet()) {
            if (compared.containsKey(e.getValue())) {
                continue;
            }

            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();

            JsonObject inspectingObject = e.getValue();
            Map<String, Object> inspecting = convert(StorageUtil.gson.fromJson(inspectingObject, mapType));
            JsonObject lastSavedObject = lastSave.get(e.getKey());
            JsonObject lastLoadedObject = lastLoad.get(e.getKey());
            if(lastSavedObject != null) {
                Map<String, Object> loaded = convert(StorageUtil.gson.fromJson(lastSavedObject, mapType));
                MapDifference<String, Object> diff = Maps.difference(inspecting, loaded);
                if(!diff.areEqual()) {
                    compared.put(e.getValue(), ComparatorAction.UPDATE);
                    continue;
                }
            }
            if(lastLoadedObject != null) {
                Map<String, Object> loaded = convert(StorageUtil.gson.fromJson(lastLoadedObject, mapType));
                MapDifference<String, Object> diff = Maps.difference(inspecting, loaded);
                if(!diff.areEqual()) {
                    compared.put(e.getValue(), ComparatorAction.UPDATE);
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

    private Map<String, Object> convert(Map<String, Object> map) {
        Map<String, Object> temp = new HashMap<>();
        for(Map.Entry<String, Object> e : map.entrySet()) {
            if(e.getValue() instanceof Number) {
                double number = (Double) e.getValue();
                if(number == Math.floor(number) && !Double.isInfinite(number)) {
                    temp.put(e.getKey(), ((Double) number).intValue());
                }
            }
        }
        for(Map.Entry<String, Object> e : temp.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
}
