package com.noahhusby.lib.data.sql.structure;

import lombok.Getter;

/**
 * @author Noah Husby
 */
public enum Type {
    INT("INT"),

    TEXT("TEXT(255)");

    @Getter private final String query;

    Type(String query) {
        this.query = query;
    }
}
