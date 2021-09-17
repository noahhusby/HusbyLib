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

package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.util.HashMap;
import java.util.Map;

public class CutComparator implements Comparator {
    @Override
    public CompareResult save(JsonArray array, StorageHandler handler) {
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        for (JsonElement je : array) {
            compared.put(je.getAsJsonObject(), ComparatorAction.ADD);
        }

        return new CompareResult(array, compared, null, true);
    }

    @Override
    public CompareResult load(JsonArray save, StorageHandler handler) {
        JsonArray array = handler.load();
        Map<JsonObject, ComparatorAction> compared = new HashMap<>();
        for (JsonElement je : array) {
            compared.put(je.getAsJsonObject(), ComparatorAction.ADD);
        }

        return new CompareResult(array, compared, null, true);
    }
}
