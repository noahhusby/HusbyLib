package com.noahhusby.lib.data.storage.events;

import com.noahhusby.lib.data.storage.Storage;
import lombok.NonNull;

/**
 * @author Noah Husby
 */
public class Event<T> {
    protected final Storage<T> storage;

    public Event(@NonNull Storage<T> storage) {
        this.storage = storage;
    }

    @NonNull
    public Storage<T> getStorage() {
        return storage;
    }
}
