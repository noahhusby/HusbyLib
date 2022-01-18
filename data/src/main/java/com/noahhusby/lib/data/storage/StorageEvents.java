package com.noahhusby.lib.data.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Noah Husby
 */
public class StorageEvents {
    protected final List<Runnable> saveEvents = new ArrayList<>();
    protected final List<Runnable> loadEvents = new ArrayList<>();

    public void onSaveEvent(Runnable e) {
        saveEvents.add(e);
    }

    public void onLoadEvent(Runnable e) {
        loadEvents.add(e);
    }
}
