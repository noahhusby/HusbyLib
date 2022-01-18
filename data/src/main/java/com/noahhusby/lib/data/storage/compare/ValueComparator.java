/*
 * MIT License
 *
 * Copyright 2020-2021 noahhusby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.noahhusby.lib.data.storage.compare;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.StorageActions;
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValueComparator<T> implements Comparator<T> {
    protected Map<Object, T> lastSave = null;
    protected Map<Object, T> lastLoad = null;
    private StorageHandler<T> handler;

    public ValueComparator(StorageHandler<T> handler) {
        this.handler = handler;
    }

    @Override
    public void save(StorageActions<T> actions) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        Map<Object, T> keyedSave = getKeyedArray(handler.getStorage().actions().get());
        Map<Object, T> keyedLoad = getKeyedArray(actions.get());

        if (lastSave == null) {
            // Since the parent list has never been saved before, we will all of the current keys against the stored data and add what is necessary.
            for (Map.Entry<Object, T> e : keyedSave.entrySet()) {
                if (!containsKey(e.getKey(), keyedLoad.keySet())) {
                    actions.add(e.getValue());
                }
            }
        } else {
            MapDifference<Object, Object> savedDifference = Maps.difference(lastSave, keyedSave);
            for (Map.Entry<Object, Object> e : savedDifference.entriesOnlyOnLeft().entrySet()) {
                if (containsKey(e.getKey(), keyedLoad.keySet())) {
                    actions.remove(toType(e.getValue()));
                }
            }

            for (Map.Entry<Object, Object> e : savedDifference.entriesOnlyOnRight().entrySet()) {
                if (!containsKey(e.getKey(), keyedLoad.keySet())) {
                    actions.add(toType(e.getValue()));
                }
            }

            for (Map.Entry<Object, MapDifference.ValueDifference<Object>> e : savedDifference.entriesDiffering().entrySet()) {
                actions.update(toType(e.getValue().rightValue()));
            }
        }

        lastSave = keyedSave;
    }

    @Override
    public void load(StorageActions<T> actions) {
        Map<Object, T> keyedSave = getKeyedArray(actions.get());
        Map<Object, T> keyedLoad = getKeyedArray(handler.actions().get());

        if (lastLoad == null) {
            // Since the parent list has never been saved before, we will all of the current keys against the stored data and add what is necessary.
            for (Map.Entry<Object, T> e : keyedLoad.entrySet()) {
                if (!containsKey(e.getKey(), keyedSave.keySet())) {
                    actions.add(e.getValue());
                }
            }
        } else {
            MapDifference<Object, Object> loadedDifference = Maps.difference(lastLoad, keyedLoad);
            for (Map.Entry<Object, Object> e : loadedDifference.entriesOnlyOnLeft().entrySet()) {
                if (containsKey(e.getKey(), keyedSave.keySet())) {
                    actions.remove(toType(e.getValue()));
                }
            }

            for (Map.Entry<Object, Object> e : loadedDifference.entriesOnlyOnRight().entrySet()) {
                if (!containsKey(e.getKey(), keyedSave.keySet())) {
                    actions.add(toType(e.getValue()));
                }
            }

            for (Map.Entry<Object, MapDifference.ValueDifference<Object>> e : loadedDifference.entriesDiffering().entrySet()) {
                actions.update(toType(e.getValue().rightValue()));
            }
        }

        lastLoad = keyedLoad;
    }


    protected Map<Object, T> getKeyedArray(Collection<T> objects) {
        Map<Object, T> temp = new HashMap<>();
        for (T obj : objects) {
            try {
                Field field =  obj.getClass().getField(handler.getStorage().getKey());
                field.setAccessible(true);
                Object key = field.get(obj);
                temp.put(key, obj);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    protected boolean containsKey(Object key, Set<Object> elements) {
        for (Object e : elements) {
            if (key.equals(e)) {
                return true;
            }
        }
        return false;
    }

    private T toType(Object o) {
        return StorageUtil.gson.fromJson(StorageUtil.gson.toJsonTree(o), handler.getStorage().getClassType());
    }
}
