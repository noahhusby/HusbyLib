package com.noahhusby.lib.data.storage.compare;

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
    public CompareResult save(JsonArray array) {
        CompareResult result = save(array, false);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        compared.putAll(result.getComparedOutput());

        for(JsonElement je : array) {
            for(JsonElement l : lastUpdate) {
                if(l.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(l.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key)))
                    for(String elementKey : je.getAsJsonObject().keySet())
                        if(l.getAsJsonObject().get(elementKey) != null &&
                                !l.getAsJsonObject().get(elementKey).equals(je.getAsJsonObject().get(elementKey)))
                            compared.put(je.getAsJsonObject(), ComparatorAction.UPDATE);
            }
        }


        lastUpdate = array.deepCopy();
        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }

    @Override
    public CompareResult load(StorageHandler handler) {
        CompareResult result = load(handler, false);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        compared.putAll(result.getComparedOutput());

        for(JsonElement je : result.getRawOutput()) {
            for(JsonElement l : lastUpdate) {
                if(l.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(l.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key)))
                    for(String elementKey : je.getAsJsonObject().keySet())
                        if(l.getAsJsonObject().get(elementKey) != null &&
                                !l.getAsJsonObject().get(elementKey).equals(je.getAsJsonObject().get(elementKey)))
                            compared.put(je.getAsJsonObject(), ComparatorAction.UPDATE);
            }
        }

        lastUpdate = result.getRawOutput().deepCopy();
        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }
}
