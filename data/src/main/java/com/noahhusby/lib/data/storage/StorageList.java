package com.noahhusby.lib.data.storage;

import com.google.gson.*;
import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;
import com.noahhusby.lib.data.storage.compare.CutComparator;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StorageList<E> extends ArrayList<E> implements Storage {

    private Comparator comparator = new CutComparator();
    private Gson gson;
    private Object E;

    public StorageList(Class<E> clazz) {
        this.E = clazz;
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .serializeNulls()
                .create();
    }

    private final List<StorageHandler> storageHandlers = new ArrayList<>();
    private ScheduledExecutorService autoSave = null;
    private ScheduledExecutorService autoLoad = null;

    @Override
    public void registerHandler(StorageHandler handler) {
        storageHandlers.add(handler);
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
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator getComparator() {
        return comparator;
    }

    @Override
    public void load() {
        try {
            if(storageHandlers.isEmpty()) return;
            StorageHandler handler = storageHandlers.get(0);

            for(StorageHandler s : storageHandlers)
                if(s.getPriority() < handler.getPriority()) handler = s;

            for(StorageHandler s : storageHandlers)
                if(s.getPriority() > handler.getPriority() && s.isAvailable()) handler = s;

            CompareResult result = comparator.load(handler);
            if(result.isCleared()) this.clear();

            for(Map.Entry<JsonObject, ComparatorAction> r : result.getComparedOutput().entrySet()) {
                if(r.getValue() == ComparatorAction.ADD) {
                    this.add(gson.fromJson(r.getKey(), (Type) E));
                }

                if(r.getValue() == ComparatorAction.REMOVE) {
                    JsonObject object = r.getKey();
                    this.removeIf(E -> gson.fromJson(gson.toJson(E), JsonObject.class)
                            .get(result.getKey()).equals(object.get(result.getKey())));
                }

                if(r.getValue() == ComparatorAction.UPDATE) {
                    JsonObject object = r.getKey();
                    JsonObject updateObject = null;
                    int val = -1;
                    for(int i = 0; i < this.size(); i++) {
                        JsonObject temp = JsonParser.parseString(new Gson().toJson(get(i))).getAsJsonObject();
                        if(temp.get(result.getKey()).equals(object.get(result.getKey()))) {
                            updateObject = temp;
                            val = i;
                            break;
                        }
                    }

                    for(String updateKey : object.keySet()) {
                        updateObject.remove(updateKey);
                        updateObject.add(updateKey, object.get(updateKey));
                    }

                    remove(val);
                    add(new Gson().fromJson(updateObject, (Type) E));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(boolean async) {
        if(async) {
            new Thread(this::load).start();
            return;
        }

        load();
    }

    @Override
    public void save() {
        JsonArray array = new JsonArray();
        for(E o : this) {
            array.add(JsonParser.parseString(gson.toJson(o)));
        }

        CompareResult result = comparator.save(array);
        for(StorageHandler s : storageHandlers)
            s.save(result);

    }

    @Override
    public void save(boolean async) {
        if(async)
            new Thread(this::save).start();
        else
            save();
    }

    @Override
    public void setAutoLoad(long period, TimeUnit unit) {
        autoLoad = Executors.newScheduledThreadPool(1);
        if(period <= 0) return;
        autoLoad.scheduleAtFixedRate(this::load, 0, period, unit);
    }

    @Override
    public void setAutoSave(long period, TimeUnit unit) {
        autoSave = Executors.newScheduledThreadPool(1);
        if(period <= 0) return;
        autoSave.scheduleAtFixedRate(this::save, 0, period, unit);
    }

    @Override
    public void migrate() {
        migrate(MigrateMode.HIGHEST_PRIORITY);
    }

    @Override
    public void migrate(MigrateMode mode) {
        int priority = storageHandlers.get(0).getPriority();
        switch (mode) {
            case HIGHEST_AVAILABLE_PRIORITY:
                for(StorageHandler handler : storageHandlers)
                    if(handler.getPriority() > priority && handler.isAvailable()) priority = handler.getPriority();
                break;
            case MOST_COMMON_DATA:
                break;
            case HIGHEST_PRIORITY:
                for(StorageHandler handler : storageHandlers)
                    if(handler.getPriority() > priority) priority = handler.getPriority();
                break;
        }
        migrate(priority);
    }

    @Override
    public void migrate(int priority) {
        StorageHandler handler = null;
        try {
            for(StorageHandler s : storageHandlers)
                if(s.getPriority() == priority) handler = s;

            if(handler == null) throw new HandlerNotAvailableExcpetion(priority);
        } catch (HandlerNotAvailableExcpetion e) {
            e.printStackTrace();
        }

        JsonArray data = handler.load();
        for(StorageHandler s : storageHandlers)
            if(s != handler) s.save(comparator.save(data));
    }
}
