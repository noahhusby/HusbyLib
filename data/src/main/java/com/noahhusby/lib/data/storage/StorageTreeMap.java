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
import com.noahhusby.lib.data.storage.handlers.StorageHandler;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Noah Husby
 */
public class StorageTreeMap<K, V> extends TreeMap<K, V> implements Storage {
    private final String key;
    private final Map<StorageHandler, Comparator> storageHandlers = new HashMap<>();
    private Object K;
    private Object V;

    private Gson gson = StorageUtil.excludedGson;

    public StorageTreeMap(Class<K> kClazz, Class<V> vClazz, java.util.Comparator<K> comparator) {
        super(comparator);
        this.K = kClazz;
        this.V = vClazz;
        key = StorageUtil.getKeyAnnotation(vClazz);
    }

    public StorageTreeMap(Class<K> kClazz, Class<V> vClazz) {
        this.K = kClazz;
        this.V = vClazz;
        key = StorageUtil.getKeyAnnotation(vClazz);
    }

    private final List<Runnable> saveEvents = new ArrayList<>();
    private final List<Runnable> loadEvents = new ArrayList<>();

    private ScheduledExecutorService autoSave = null;
    private ScheduledExecutorService autoLoad = null;

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
                if (r.getValue() == ComparatorAction.ADD) {
                    JsonObject value = r.getKey();
                    JsonElement keyValue = value.get(key);
                    this.put(gson.fromJson(keyValue, (Type) K), gson.fromJson(value, (Type) V));
                }

                if (r.getValue() == ComparatorAction.REMOVE) {
                    JsonObject value = r.getKey();
                    K keyObject = gson.fromJson(value.get(key), (Type) K);
                    this.remove(keyObject);
                }

                if (r.getValue() == ComparatorAction.UPDATE) {
                    JsonObject value = r.getKey();
                    JsonElement keyValue = value.get(key);
                    this.put(gson.fromJson(keyValue, (Type) K), gson.fromJson(value, (Type) V));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadEvents.forEach(Runnable::run);
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
            saveEvents.forEach(Runnable::run);
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
    public void migrate() {
        migrate(MigrateMode.HIGHEST_PRIORITY);
    }

    @Override
    public void migrate(MigrateMode mode) {
        int priority = new ArrayList<>(storageHandlers.keySet()).get(0).getPriority();
        switch (mode) {
            case HIGHEST_AVAILABLE_PRIORITY:
                for (StorageHandler handler : storageHandlers.keySet()) {
                    if (handler.getPriority() > priority && handler.isAvailable()) {
                        priority = handler.getPriority();
                    }
                }
                break;
            case MOST_COMMON_DATA:
                break;
            case HIGHEST_PRIORITY:
                for (StorageHandler handler : storageHandlers.keySet()) {
                    if (handler.getPriority() > priority) {
                        priority = handler.getPriority();
                    }
                }
                break;
        }
        migrate(priority);
    }

    @Override
    public void migrate(int priority) {
        StorageHandler handler = null;
        try {
            for (StorageHandler s : storageHandlers.keySet()) {
                if (s.getPriority() == priority) {
                    handler = s;
                }
            }

            if (handler == null) {
                throw new HandlerNotAvailableExcpetion(priority);
            }
        } catch (HandlerNotAvailableExcpetion e) {
            e.printStackTrace();
            return;
        }

        JsonArray data = handler.load();
        if (data == null) {
            return;
        }
        for (StorageHandler s : storageHandlers.keySet()) {
            if (s != handler) {
                s.save(storageHandlers.get(s).save(data, s));
            }
        }
    }

    @Override
    public void onLoadEvent(Runnable runnable) {
        loadEvents.add(runnable);
    }

    @Override
    public void onSaveEvent(Runnable runnable) {
        saveEvents.add(runnable);
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
        for (Map.Entry<K, V> e : this.entrySet()) {
            JsonObject value = gson.toJsonTree(e.getValue()).getAsJsonObject();
            if (!value.has(key)) {
                value.add(key, gson.toJsonTree(e.getKey()));
            }
            array.add(value);
        }
        return array;
    }

}
