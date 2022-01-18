package com.noahhusby.lib.data.storage;

/**
 * @author Noah Husby
 */
public abstract class StorageActions<T> {
    public abstract void add(T o);

    public abstract void remove(T o);

    public abstract void update(T o);
}
