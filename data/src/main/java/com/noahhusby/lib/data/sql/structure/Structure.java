/*
 * MIT License
 *
 * Copyright 2020-2021 noahhusby
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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
