package com.noahhusby.lib.data.sql.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author Noah Husby
 */
@AllArgsConstructor
public class StructureElement {
    @Getter
    private final String column;
    @Getter
    private final List<String> keys;
    @Getter
    private final Type type;
}
