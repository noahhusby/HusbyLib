package com.noahhusby.lib.data.storage;

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
        int priority = new ArrayList<>(storage.handlers().getHandlers()).get(0).getPriority();
        for (StorageHandler<?> handler : storage.handlers().getHandlers()) {
            if (handler.getPriority() > priority) {
                priority = handler.getPriority();
            }
        }
        migrate(priority);
    }

    public void highestAvailable() {
        int priority = new ArrayList<>(storage.handlers().getHandlers()).get(0).getPriority();
        for (StorageHandler<?> handler : storage.handlers().getHandlers()) {
            if (handler.getPriority() > priority && handler.isAvailable()) {
                priority = handler.getPriority();
            }
        }
        migrate(priority);
    }

    public void migrate(int priority) {
        StorageHandler<?> handler = null;
        try {
            for (StorageHandler<?> s : storage.handlers().getHandlers()) {
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
    }
}
