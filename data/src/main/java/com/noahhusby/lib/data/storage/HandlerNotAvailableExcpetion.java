package com.noahhusby.lib.data.storage;

public class HandlerNotAvailableExcpetion extends Exception {
    public HandlerNotAvailableExcpetion(int priority) {
        super("No handler available at priority: " + priority);
    }
}
