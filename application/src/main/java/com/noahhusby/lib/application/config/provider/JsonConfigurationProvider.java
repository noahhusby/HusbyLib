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
