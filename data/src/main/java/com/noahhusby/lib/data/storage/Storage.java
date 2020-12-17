package com.noahhusby.lib.data.storage;

import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.concurrent.TimeUnit;

public interface Storage {
    void registerHandler(StorageHandler handler);
    void unregisterHandler(StorageHandler handler);
    void clearHandlers();

    void setComparator(Comparator comparator);
    Comparator getComparator();

    void load();
    void load(boolean async);
    void save();
    void save(boolean async);

    void setAutoLoad(long period, TimeUnit unit);
    void setAutoSave(long period, TimeUnit unit);

    void migrate();
    void migrate(MigrateMode mode);
    void migrate(int priority);
}
