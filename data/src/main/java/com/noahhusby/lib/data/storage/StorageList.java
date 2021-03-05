package com.noahhusby.lib.data.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.JsonUtils;
import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;
import com.noahhusby.lib.data.storage.compare.CutComparator;
import com.noahhusby.lib.data.storage.compare.ValueComparator;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StorageList<E> extends ArrayList<E> implements Storage {

    private final String key;
    private final Map<StorageHandler, Comparator> storageHandlers = new HashMap<>();
    private Object E;

    private Gson gson = StorageUtil.excludedGson;

    public StorageList(Class<E> clazz) {
        this.E = clazz;
        key = StorageUtil.getKeyAnnotation(clazz);
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

            CompareResult result = storageHandlers.get(handler).load(handler);
            if (result.isCleared()) {
                this.clear();
            }

            for (Map.Entry<JsonObject, ComparatorAction> r : result.getComparedOutput().entrySet()) {
                if (r.getValue() == ComparatorAction.ADD) {
                    this.add(gson.fromJson(r.getKey(), (Type) E));
                }

                if (r.getValue() == ComparatorAction.REMOVE) {
                    JsonObject object = r.getKey();
                    this.removeIf(E -> gson.fromJson(gson.toJson(E), JsonObject.class)
                            .get(result.getKey()).equals(object.get(result.getKey())));
                }

                if (r.getValue() == ComparatorAction.UPDATE) {
                    JsonObject object = r.getKey();
                    JsonObject updateObject = null;
                    int val = -1;
                    for (int i = 0; i < this.size(); i++) {
                        JsonObject temp = JsonUtils.parseString(new Gson().toJson(get(i))).getAsJsonObject();
                        if (temp.get(result.getKey()).equals(object.get(result.getKey()))) {
                            updateObject = temp;
                            val = i;
                            break;
                        }
                    }

                    if (updateObject != null) {
                        for (String updateKey : JsonUtils.keySet(object)) {

                            updateObject.remove(updateKey);
                            updateObject.add(updateKey, object.get(updateKey));
                        }

                        remove(val);
                        add(new Gson().fromJson(updateObject, (Type) E));
                    }
                }
            }

            // Duplicate check
            if (key != null) {
                Map<JsonElement, JsonObject> keyedDuplicate = new HashMap<>();
                for (JsonElement o : result.getRawOutput()) {
                    JsonObject object = o.getAsJsonObject();
                    keyedDuplicate.put(object.get(key), object);
                }
                List<E> removeDuplicates = new ArrayList<>();
                for (E e : this) {
                    JsonObject object = gson.toJsonTree(e).getAsJsonObject();
                    JsonElement objectKey = object.get(key);
                    if (objectKey == null) {
                        continue;
                    }
                    JsonObject correct = keyedDuplicate.get(objectKey);
                    if (correct == null) {
                        continue;
                    }
                    // Re-serialize to check custom rules
                    object = gson.toJsonTree(gson.fromJson(object, (Type) E)).getAsJsonObject();
                    correct = gson.toJsonTree(gson.fromJson(correct, (Type) E)).getAsJsonObject();
                    for (String elementKey : JsonUtils.keySet(correct)) {
                        if (object.get(elementKey) == null || !object.get(elementKey).equals(correct.get(elementKey))) {
                            removeDuplicates.add(e);
                            break;
                        }
                    }
                }
                removeIf(removeDuplicates::contains);
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
        JsonArray array = new JsonArray();
        for (E o : this) {
            array.add(gson.toJsonTree(o));
        }
        for (Map.Entry<StorageHandler, Comparator> e : storageHandlers.entrySet()) {
            e.getKey().save(e.getValue().save(array));
        }
        saveEvents.forEach(Runnable::run);
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
        }

        JsonArray data = handler.load();
        if(data == null) return;
        for (StorageHandler s : storageHandlers.keySet()) {
            if (s != handler) {
                s.save(storageHandlers.get(s).save(data));
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

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected void finalize() throws Throwable {
        if(autoLoad != null) {
            autoLoad.shutdownNow();
            autoLoad = null;
        }
        if(autoSave != null) {
            autoSave.shutdownNow();
            autoSave = null;
        }
        super.finalize();
    }
}
