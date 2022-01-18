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

package com.noahhusby.lib.data.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;
import com.noahhusby.lib.data.storage.compare.CutComparator;
import com.noahhusby.lib.data.storage.compare.ValueComparator;
import com.noahhusby.lib.data.storage.events.action.StorageAddEvent;
import com.noahhusby.lib.data.storage.events.action.StorageRemoveEvent;
import com.noahhusby.lib.data.storage.events.action.StorageUpdateEvent;
import com.noahhusby.lib.data.storage.events.transfer.StorageLoadEvent;
import com.noahhusby.lib.data.storage.events.transfer.StorageSaveEvent;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Noah Husby
 */
public class StorageHashMap<K, V> extends HashMap<K, V> implements Storage<V> {
    private final String key;
    private final Map<StorageHandler, Comparator> storageHandlers = new HashMap<>();
    private Object K;
    private Object V;

    private Gson gson = StorageUtil.excludedGson;

    public StorageHashMap(Class<K> kClazz, Class<V> vClazz) {
        this.K = kClazz;
        this.V = vClazz;
        key = StorageUtil.getKeyAnnotation(vClazz);
    }

    private ScheduledExecutorService autoSave = null;
    private ScheduledExecutorService autoLoad = null;

    private final StorageActions<V> actions = new StorageActions<V>() {
        @Override
        public void add(V o) {
            try {
                K objKey = (K) o.getClass().getField(key).get(o);
                put(objKey, o);
                events.listeners.forEach(l -> l.onAddAction(new StorageAddEvent<>(StorageHashMap.this, o)));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void remove(V o) {
            try {
                K objKey = (K) o.getClass().getField(key).get(o);
                StorageHashMap.this.remove(objKey);
                events.listeners.forEach(l -> l.onRemoveAction(new StorageRemoveEvent<>(StorageHashMap.this, o)));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void update(V o) {
            try {
                K objKey = (K) o.getClass().getField(key).get(o);
                put(objKey, o);
                events.listeners.forEach(l -> l.onUpdateAction(new StorageUpdateEvent<>(StorageHashMap.this, o)));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    };

    private final StorageEvents<V> events = new StorageEvents<>();
    private final StorageMigration migrate = new StorageMigration(this);

    @Override
    public void registerHandler(StorageHandler handler) {
        storageHandlers.put(handler, key == null ? new CutComparator() : new ValueComparator(key));
    }

    @Override
    public void registerHandler(StorageHandler handler, Comparator comparator) {
        storageHandlers.put(handler, comparator);
    }

    @Override
    public Map<StorageHandler, Comparator> getHandlers() {
        return storageHandlers;
    }

    @Override
    public void unregisterHandler(StorageHandler handler) {
        storageHandlers.remove(handler);
    }

    @Override
    public void clearHandlers() {
        storageHandlers.clear();
    }

    @Override
    public void load() {
        try {
            if (storageHandlers.isEmpty()) {
                return;
            }

            List<StorageHandler> handlers = new ArrayList<>(storageHandlers.keySet());
            StorageHandler handler = handlers.get(0);

            for (StorageHandler s : handlers) {
                if (s.getPriority() < handler.getPriority()) {
                    handler = s;
                }
            }

            for (StorageHandler s : handlers) {
                if (s.getPriority() > handler.getPriority() && s.isAvailable()) {
                    handler = s;
                }
            }

            CompareResult result = storageHandlers.get(handler).load(getSaveData(), handler);
            if (result.isCleared()) {
                this.clear();
            }

            for (Map.Entry<JsonObject, ComparatorAction> r : result.getComparedOutput().entrySet()) {
                if (r.getValue() == ComparatorAction.ADD || r.getValue() == ComparatorAction.UPDATE) {
                    actions.add(gson.fromJson(r.getKey(), (Type) V));
                }

                if (r.getValue() == ComparatorAction.REMOVE) {
                    actions.remove(gson.fromJson(r.getKey(), (Type) V));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        events.listeners.forEach(l -> l.onLoad(new StorageLoadEvent<>(this)));
    }

    @Override
    public void loadAsync() {
        new Thread(this::load).start();
    }

    @Override
    public void save() {
        try {
            JsonArray array = getSaveData();
            for (Map.Entry<StorageHandler, Comparator> e : storageHandlers.entrySet()) {
                e.getKey().save(e.getValue().save(array, e.getKey()));
            }
            events.listeners.forEach(l -> l.onSave(new StorageSaveEvent<>(this)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAsync() {
        new Thread(this::save).start();
    }

    @Override
    public void setAutoLoad(long period, TimeUnit unit) {
        autoLoad = Executors.newScheduledThreadPool(1);
        if (period <= 0) {
            return;
        }
        autoLoad.scheduleAtFixedRate(this::load, 0, period, unit);
    }

    @Override
    public void setAutoSave(long period, TimeUnit unit) {
        autoSave = Executors.newScheduledThreadPool(1);
        if (period <= 0) {
            return;
        }
        autoSave.scheduleAtFixedRate(this::save, 0, period, unit);
    }

    @Override
    public StorageMigration migrate() {
        return migrate;
    }

    @Override
    public StorageEvents<V> events() {
        return events;
    }

    @Override
    public StorageActions<V> actions() {
        return actions;
    }

    @SneakyThrows
    @Override
    public void close() {
        if (autoLoad != null) {
            autoLoad.shutdownNow();
            autoLoad = null;
        }
        if (autoSave != null) {
            autoSave.shutdownNow();
            autoSave = null;
        }
        for (StorageHandler handler : storageHandlers.keySet()) {
            handler.close();
        }
        storageHandlers.clear();
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    private JsonArray getSaveData() {
        JsonArray array = new JsonArray();
        for (Entry<K, V> e : this.entrySet()) {
            JsonObject value = gson.toJsonTree(e.getValue()).getAsJsonObject();
            if (!value.has(key)) {
                value.add(key, gson.toJsonTree(e.getKey()));
            }
            array.add(value);
        }
        return array;
    }

}
