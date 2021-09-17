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

package com.noahhusby.lib.application.config.provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.noahhusby.lib.application.config.Property;
import com.noahhusby.lib.application.config.source.ConfigurationSource;
import com.noahhusby.lib.common.util.HusbyUtil;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class JsonConfigurationProvider extends ConfigurationProvider {

    @Builder(setterPrefix = "set")
    public JsonConfigurationProvider(@NonNull ConfigurationSource source) {
        super(source);
    }

    @Override
    @SneakyThrows
    public void load() {
        File file = source.getFile();
        @Cleanup Reader reader = Files.newBufferedReader(file.toPath());
        entries = HusbyUtil.GSON.fromJson(reader, Map.class);
        if (entries == null) {
            entries = new HashMap<>();
        }
    }


    @Override
    @SneakyThrows
    public void update(Map<String, Property> properties) {
        load();
        JsonObject output = new JsonObject();
        boolean changedOutput = false;
        // Add all loaded entries
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            output.add((String) entry.getKey(), HusbyUtil.GSON.toJsonTree(entry.getValue()));
        }
        // Add all missing entries
        for (Map.Entry<String, Property> property : properties.entrySet()) {
            if (!output.has(property.getKey())) {
                Object value = property.getValue().getValue();
                JsonElement element = value == null ? JsonNull.INSTANCE : HusbyUtil.GSON.toJsonTree(value);
                output.add(property.getKey(), element);
                changedOutput = true;
            }
        }
        // Remove all unnecessary entries
        for (Map.Entry<String, JsonElement> entry : new ArrayList<>(output.entrySet())) {
            if (!properties.containsKey(entry.getKey())) {
                output.remove(entry.getKey());
                changedOutput = true;
            }
        }
        if (changedOutput) {
            FileWriter writer = new FileWriter(getSource().getFile());
            HusbyUtil.GSON.toJson(output, writer);
            writer.close();
            load();
        }
    }
}
