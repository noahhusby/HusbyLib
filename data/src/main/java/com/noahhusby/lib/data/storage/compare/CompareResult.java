package com.noahhusby.lib.data.storage.compare;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

public class CompareResult {
    private final JsonArray raw;
    private final Map<JsonObject, ComparatorAction> compared;
    private final boolean clear;
    private final String key;

    public CompareResult(JsonArray raw, Map<JsonObject, ComparatorAction> compared, String key, boolean clear) {
        this.raw = raw;
        this.compared = compared;
        this.key = key;
        this.clear = clear;
    }

    public Map<JsonObject, ComparatorAction> getComparedOutput() {
        return compared;
    }

    public JsonArray getRawOutput() {
        return raw;
    }

    public boolean isCleared() {
        return clear;
    }

    public String getKey() {
        return key;
    }
}
