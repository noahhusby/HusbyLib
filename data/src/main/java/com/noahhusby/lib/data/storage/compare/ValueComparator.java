package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

        for(JsonElement je : array) {
            if(je.getAsJsonObject().get(key) == null) continue;
            boolean update = false;
            for(JsonElement l : lastLoad) {
                if(l.getAsJsonObject().get(key) == null) continue;
                if(l.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    for(String elementKey : JsonUtils.keySet(je.getAsJsonObject())) {
                        if(l.getAsJsonObject().get(elementKey) != null &&
                                !l.getAsJsonObject().get(elementKey).equals(je.getAsJsonObject().get(elementKey))) {
                            update = true;
                            break;
                        }
                    }
                }
            }

            if(!update) {
                for(JsonElement l : lastSave) {
                    if(l.getAsJsonObject().get(key) == null) continue;
                    if(l.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                        for(String elementKey : JsonUtils.keySet(je.getAsJsonObject())) {
                            if(l.getAsJsonObject().get(elementKey) != null &&
                                    !l.getAsJsonObject().get(elementKey).equals(je.getAsJsonObject().get(elementKey))) {
                                update = true;
                                break;
                            }
                        }
                    }
                }
            }

            if(update)
                compared.put(je.getAsJsonObject(), ComparatorAction.UPDATE);
        }

        try {
            Method m = JsonArray.class.getDeclaredMethod("deepCopy");
            m.setAccessible(true);
            lastSave = (JsonArray) m.invoke(array);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }

    @Override
    public CompareResult load(StorageHandler handler) {
        CompareResult result = load(handler, false);
        Map<JsonObject, ComparatorAction> compared = new HashMap<>(result.getComparedOutput());

        for(JsonElement je : result.getRawOutput()) {
            for(JsonElement l : lastSave) {
                if(l.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(l.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key)))
                    for(String elementKey : JsonUtils.keySet(je.getAsJsonObject()))
                        if(l.getAsJsonObject().get(elementKey) != null &&
                                !l.getAsJsonObject().get(elementKey).equals(je.getAsJsonObject().get(elementKey)))
                            compared.put(je.getAsJsonObject(), ComparatorAction.UPDATE);
            }
        }

        try {
            Method m = JsonArray.class.getDeclaredMethod("deepCopy");
            m.setAccessible(true);
            lastLoad = (JsonArray) m.invoke(result.getRawOutput());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return new CompareResult(result.getRawOutput(), compared, key, result.isCleared());
    }
}
