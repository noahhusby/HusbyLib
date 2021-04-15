package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

public interface Comparator {
    CompareResult save(JsonArray array, StorageHandler handler);

    CompareResult load(JsonArray array, StorageHandler handler);
}
