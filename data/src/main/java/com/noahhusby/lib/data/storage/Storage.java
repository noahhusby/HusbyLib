package com.noahhusby.lib.data.storage;

import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface Storage {
    void registerHandler(StorageHandler handler);

    void registerHandler(StorageHandler handler, Comparator comparator);

    Map<StorageHandler, Comparator> getHandlers();

    void unregisterHandler(StorageHandler handler);

    void clearHandlers();

    void load();

    void loadAsync();

    void save();

    void saveAsync();

    void setAutoLoad(long period, TimeUnit unit);

    void setAutoSave(long period, TimeUnit unit);

    void migrate();

    void migrate(MigrateMode mode);

    void migrate(int priority);

    void onLoadEvent(Runnable runnable);

    void onSaveEvent(Runnable runnable);
}
