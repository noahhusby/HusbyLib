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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.noahhusby.lib.application.config.Configuration;
import com.noahhusby.lib.application.config.Property;
import com.noahhusby.lib.application.config.source.ConfigurationSource;
import com.noahhusby.lib.common.util.HusbyUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class HoconConfigurationProvider extends ConfigurationProvider {

    @Builder(setterPrefix = "set")
    public HoconConfigurationProvider(@NonNull ConfigurationSource source) {
        super(source);
    }

    @SneakyThrows
    @Override
    public void load() {
        File file = source.getFile();
        @Cleanup Reader reader = Files.newBufferedReader(file.toPath());
        Config config = ConfigFactory.parseReader(reader);
        entries = config.root().unwrapped();
    }

    @Override
    @SneakyThrows
    public void update(Map<String, Property> properties) {
        load();
        Map<String, Property> output = new LinkedHashMap<>(properties);
        for (String key : new LinkedList<>(properties.keySet())) {
            if (entries.containsKey(key)) {
                Property old = properties.get(key);
                Object newValue = HusbyUtil.GSON.fromJson(HusbyUtil.GSON.toJson(entries.get(key)), old.getField().getType());
                output.put(key, new Property(old.getName(), old.getComment(), newValue, old.getField(), old.getEnvironmentVariable()));
            }
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(getSource().getFile()));
        writer.write("# Configuration File\n\n");
        writeProperties(writer, output);
        writer.close();
        load();
    }

    @SneakyThrows
    private void writeProperties(BufferedWriter writer, Map<String, Property> properties) {
        writeProperties(writer, properties, "");
    }

    @SneakyThrows
    private void writeProperties(BufferedWriter writer, Map<String, Property> properties, String indent) {
        for (Map.Entry<String, Property> property : properties.entrySet()) {
            JsonElement element = HusbyUtil.GSON.toJsonTree(property.getValue().getValue());
            if (element instanceof JsonObject) {
                if (property.getValue().getComment() != null) {
                    writer.write(indent + "##########################################################################################################\n");
                    writer.write(indent + "# " + property.getKey() + "\n");
                    writer.write(indent + "#--------------------------------------------------------------------------------------------------------#\n");
                    for (String comment : property.getValue().getComment()) {
                        writer.write(indent + "# " + comment + "\n");
                    }
                    writer.write(indent + "##########################################################################################################\n\n");
                }
                writer.write(indent + property.getKey() + " = {\n");
                writeProperties(writer, Configuration.getProperties(property.getValue().getValue()), indent + "\t");
                writer.write(indent + "}\n");
            } else {
                if (property.getValue().getComment() != null) {
                    for (String comment : property.getValue().getComment()) {
                        writer.write(indent + "# " + comment + "\n");
                    }
                }
                if (element instanceof JsonArray) {
                    writer.write(indent + property.getKey() + " = [\n");
                    for (JsonElement e : element.getAsJsonArray()) {
                        writer.write(indent + indent + e.toString() + "\n");
                    }
                    writer.write(indent + "]\n");
                } else {
                    if (property.getValue().getValue() instanceof String) {
                        writer.write(indent + property.getKey() + " = \"" + property.getValue().getValue() + "\"\n\n");
                    } else {
                        writer.write(indent + property.getKey() + " = " + property.getValue().getValue() + "\n\n");
                    }
                }
            }
        }
    }
}
