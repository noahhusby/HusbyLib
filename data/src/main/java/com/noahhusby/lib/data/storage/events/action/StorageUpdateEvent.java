package com.noahhusby.lib.data.storage.events.action;

import com.noahhusby.lib.data.storage.Storage;
import com.noahhusby.lib.data.storage.events.Event;
import lombok.NonNull;

/**
 * @author Noah Husby
 */
public class StorageUpdateEvent<T> extends Event<T> {

    private final T obj;

    public StorageUpdateEvent(@NonNull Storage<T> storage, @NonNull T obj) {
        super(storage);
        this.obj = obj;
    }

    public T getObject() {
        return obj;
    }
}
