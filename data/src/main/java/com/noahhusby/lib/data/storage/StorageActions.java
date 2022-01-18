package com.noahhusby.lib.data.storage;

import java.util.Collection;

/**
 * @author Noah Husby
 */
public abstract class StorageActions<T> {
    public abstract void add(T o);

    public abstract void remove(T o);

    public abstract void update(T o);

    public abstract Collection<T> get();
}
