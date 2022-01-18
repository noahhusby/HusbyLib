package com.noahhusby.lib.data.storage;

import com.noahhusby.lib.data.storage.events.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Noah Husby
 */
public class StorageEvents<T> {
    protected final List<EventListener<T>> listeners = new ArrayList<>();

    public void register(EventListener<T> listener) {
        listeners.add(listener);
    }

    public void unregister(EventListener<T> listener) {
        listeners.remove(listener);
    }
}
