package com.noahhusby.lib.data.storage;

import com.google.gson.JsonArray;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class StorageMigration {
    private final Storage<?> storage;

    public void highest() {
        int priority = new ArrayList<>(storage.getHandlers().keySet()).get(0).getPriority();
        for (StorageHandler<?> handler : storage.getHandlers().keySet()) {
            if (handler.getPriority() > priority) {
                priority = handler.getPriority();
            }
        }
        migrate(priority);
    }

    public void highestAvailable() {
        int priority = new ArrayList<>(storage.getHandlers().keySet()).get(0).getPriority();
        for (StorageHandler<?> handler : storage.getHandlers().keySet()) {
            if (handler.getPriority() > priority && handler.isAvailable()) {
                priority = handler.getPriority();
            }
        }
        migrate(priority);
    }

    public void migrate(int priority) {
        StorageHandler<?> handler = null;
        try {
            for (StorageHandler<?> s : storage.getHandlers().keySet()) {
                if (s.getPriority() == priority) {
                    handler = s;
                }
            }

            if (handler == null) {
                throw new HandlerNotAvailableExcpetion(priority);
            }
        } catch (HandlerNotAvailableExcpetion e) {
            e.printStackTrace();
            return;
        }

        JsonArray data = handler.load();
        if (data == null) {
            return;
        }
        for (StorageHandler<?> s : storage.getHandlers().keySet()) {
            if (s != handler) {
                s.save(storage.getHandlers().get(s).save(data, s));
            }
        }
    }
}
