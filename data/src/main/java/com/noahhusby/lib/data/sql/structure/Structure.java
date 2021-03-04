package com.noahhusby.lib.data.sql.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class Structure {
    @Getter
    private final List<StructureElement> elements;
    @Getter
    private final boolean repair;
    private List<String> columnNames;

    public List<String> getColumnNames() {
        if (columnNames == null) {
            columnNames = new ArrayList<>();
            for (StructureElement s : elements) {
                columnNames.add(s.getColumn());
            }
        }

        return columnNames;
    }

    public static StructureBuilder builder() {
        return new StructureBuilder();
    }

    public static class StructureBuilder {
        private List<StructureElement> elements = new ArrayList<>();
        private boolean repair;

        StructureBuilder() {
        }

        public StructureBuilder repair(boolean repair) {
            this.repair = repair;
            return this;
        }

        public StructureBuilder add(String column, Type type, String... keys) {
            List<String> keyList = new ArrayList<>(Arrays.asList(keys));
            if (keyList.size() == 0) {
                keyList.add(column);
            }
            elements.add(new StructureElement(column, keyList, type));
            return this;
        }

        public Structure build() {
            return new Structure(elements, repair);
        }
    }
}
