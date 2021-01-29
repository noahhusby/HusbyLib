package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class KeyComparator implements Comparator {
    protected JsonArray lastSave = new JsonArray();
    protected JsonArray lastLoad = new JsonArray();

    protected final String key;

    public KeyComparator(String key) {
        this.key = key;
    }

    @Override
    public CompareResult save(JsonArray array) {
        return save(array, true);
    }

    protected CompareResult save(JsonArray array, boolean save) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();

        for(JsonElement je : array) {
            boolean a = true;
            for(JsonElement ls : lastLoad) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    a = false;
                    break;
                }
            }

            if(a) {
                for(JsonElement ls : lastSave) {
                    if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                    if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                        a = false;
                        break;
                    }
                }
            }

            if(a)
                compared.put(je.getAsJsonObject(), ComparatorAction.ADD);
        }

        for(JsonElement ls : lastSave) {
            boolean r = true;
            for(JsonElement je : array) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    r = false;
                    break;
                }
            }

            if(r)
                compared.put(ls.getAsJsonObject(), ComparatorAction.REMOVE);
        }

        if(save)  {
            try {
                Method m = JsonArray.class.getDeclaredMethod("deepCopy");
                m.setAccessible(true);
                lastSave = (JsonArray) m.invoke(array);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return new CompareResult(array, compared, key, false);
    }

    @Override
    public CompareResult load(StorageHandler handler) {
        return load(handler, true);
    }

    protected CompareResult load(StorageHandler handler, boolean save) {
        JsonArray array = handler.load();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();

        for(JsonElement je : array) {
            boolean a = true;
            for(JsonElement ls : lastLoad) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    a = false;
                    break;
                }
            }

            if(a) {
                for(JsonElement ls : lastSave) {
                    if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                    if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                        a = false;
                        break;
                    }
                }
            }

            if(a)
                compared.put(je.getAsJsonObject(), ComparatorAction.ADD);
        }

        for(JsonElement ls : lastLoad) {
            boolean r = true;
            for(JsonElement je : array) {
                if(ls.getAsJsonObject().get(key) == null || je.getAsJsonObject().get(key) == null) continue;
                if(ls.getAsJsonObject().get(key).equals(je.getAsJsonObject().get(key))) {
                    r = false;
                    break;
                }
            }

            if(r)
                compared.put(ls.getAsJsonObject(), ComparatorAction.REMOVE);
        }

        if(save)  {
            try {
                Method m = JsonArray.class.getDeclaredMethod("deepCopy");
                m.setAccessible(true);
                lastLoad = (JsonArray) m.invoke(array);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return new CompareResult(array, compared, key, false);
    }
}
