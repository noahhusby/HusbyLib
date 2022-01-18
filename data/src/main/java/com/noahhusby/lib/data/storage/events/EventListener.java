package com.noahhusby.lib.data.storage.events;

import com.noahhusby.lib.data.storage.events.action.StorageAddEvent;
import com.noahhusby.lib.data.storage.events.action.StorageRemoveEvent;
import com.noahhusby.lib.data.storage.events.action.StorageUpdateEvent;
import com.noahhusby.lib.data.storage.events.transfer.StorageLoadEvent;
import com.noahhusby.lib.data.storage.events.transfer.StorageSaveEvent;

/**
 * @author Noah Husby
 */
public abstract class EventListener<T> {
    public void onAddAction(StorageAddEvent<T> event) {
    }

    public void onRemoveAction(StorageRemoveEvent<T> event) {
    }

    public void onUpdateAction(StorageUpdateEvent<T> event) {
    }

    public void onSave(StorageSaveEvent<T> event) {
    }

    public void onLoad(StorageLoadEvent<T> event) {
    }
}
