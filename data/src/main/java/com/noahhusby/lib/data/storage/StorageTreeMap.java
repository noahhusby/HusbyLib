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
import com.noahhusby.lib.data.storage.events.action.StorageAddEvent;
import com.noahhusby.lib.data.storage.events.action.StorageRemoveEvent;
import com.noahhusby.lib.data.storage.events.action.StorageUpdateEvent;
import com.noahhusby.lib.data.storage.events.transfer.StorageLoadEvent;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Noah Husby
 */
public class StorageTreeMap<K, V> extends TreeMap<K, V> implements Storage<V> {
    @Getter
    private final String key;
    private final Class<V> V;

    private Gson gson = StorageUtil.excludedGson;

    public StorageTreeMap(Class<V> vClazz, java.util.Comparator<K> comparator) {
        super(comparator);
        this.V = vClazz;
        key = StorageUtil.getKeyAnnotation(vClazz);
    }

    public StorageTreeMap(Class<V> vClazz) {
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
                events.listeners.forEach(l -> l.onAddAction(new StorageAddEvent<>(StorageTreeMap.this, o)));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void remove(V o) {
            try {
                K objKey = (K) o.getClass().getField(key).get(o);
                StorageTreeMap.this.remove(objKey);
                events.listeners.forEach(l -> l.onRemoveAction(new StorageRemoveEvent<>(StorageTreeMap.this, o)));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void update(V o) {
            try {
                K objKey = (K) o.getClass().getField(key).get(o);
                put(objKey, o);
                events.listeners.forEach(l -> l.onUpdateAction(new StorageUpdateEvent<>(StorageTreeMap.this, o)));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Collection<V> get() {
            return values();
        }
    };

    private final StorageEvents<V> events = new StorageEvents<>();
    private final StorageMigration migrate = new StorageMigration(this);
    private final StorageHandlers<V> handlers = new StorageHandlers<>(this);

    @Override
    public void load() {
        try {
            if (handlers.getHandlers().isEmpty()) {
                return;
            }

            List<StorageHandler<V>> handlers = new ArrayList<>(handlers().getHandlers());
            StorageHandler<V> handler = handlers.get(0);

            for (StorageHandler<V> s : handlers) {
                if (s.getPriority() < handler.getPriority()) {
                    handler = s;
                }
            }

            for (StorageHandler<V> s : handlers) {
                if (s.getPriority() > handler.getPriority() && s.isAvailable()) {
                    handler = s;
                }
            }

            handler.load();
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
        handlers.getHandlers().forEach(StorageHandler::save);
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
    public Class<V> getClassType() {
        return V;
    }

    @Override
    public StorageHandlers<V> handlers() {
        return handlers;
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
        handlers.getHandlers().forEach(h -> {
            try {
                h.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        handlers.getHandlers().clear();
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
