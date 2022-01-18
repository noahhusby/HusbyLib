package com.noahhusby.lib.data.storage.events.transfer;

import com.noahhusby.lib.data.storage.Storage;
import com.noahhusby.lib.data.storage.events.Event;
import lombok.NonNull;

/**
 * @author Noah Husby
 */
public class StorageSaveEvent<T> extends Event<T> {

    public StorageSaveEvent(@NonNull Storage<T> storage) {
        super(storage);
    }
}
