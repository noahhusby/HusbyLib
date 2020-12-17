package com.noahhusby.lib.data.storage;

public class TypeNotStorableException extends Exception {
    public TypeNotStorableException(Object o) {
        super(o.getClass().getName() + "does not implement Storable");
    }
}
