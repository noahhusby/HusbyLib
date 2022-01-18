package com.noahhusby.lib.data.storage;

import com.google.gson.JsonArray;
import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.compare.ValueComparator;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class StorageHandlers<T> {
    private final Storage<T> storage;

    private final ArrayList<StorageHandler<T>> handlers = new ArrayList<>();

    public void registerHandler(StorageHandler<T> handler) {
        handler.init(storage);
        handlers.add(handler);
    }

    public List<StorageHandler<T>> getHandlers() {
        return handlers;
    }

    public void unregisterHandler(StorageHandler<T> handler) {
        handlers.remove(handler);
    }

    public void clearHandlers() {
        handlers.clear();
    }
}
