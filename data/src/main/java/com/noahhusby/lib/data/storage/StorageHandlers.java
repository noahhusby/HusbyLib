package com.noahhusby.lib.data.storage;

import com.noahhusby.lib.data.storage.handlers.StorageHandler;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class StorageHandlers<T> {
    private final Storage<T> storage;

    private final ArrayList<StorageHandler<T>> handlers = new ArrayList<>();

    public void register(StorageHandler<T> handler) {
        handler.init(storage);
        handlers.add(handler);
    }

    public List<StorageHandler<T>> getHandlers() {
        return handlers;
    }

    public void unregister(StorageHandler<T> handler) {
        handlers.remove(handler);
    }

    public void clear() {
        handlers.clear();
    }
}
