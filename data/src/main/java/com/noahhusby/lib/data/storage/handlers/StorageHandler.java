package com.noahhusby.lib.data.storage.handlers;

import com.google.gson.JsonArray;
import com.noahhusby.lib.data.IDestroyable;
import com.noahhusby.lib.data.storage.Storage;
import com.noahhusby.lib.data.storage.compare.CompareResult;

public interface StorageHandler extends IDestroyable {

    void init(Storage storage);

    void save(CompareResult result);

    JsonArray load();

    int getPriority();

    void setPriority(int priority);

    boolean isAvailable();
}
