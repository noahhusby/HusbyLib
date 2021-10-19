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

import com.noahhusby.lib.application.config.Property;
import com.noahhusby.lib.application.config.source.ConfigurationSource;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Noah Husby
 */
public class YamlConfigurationProvider extends ConfigurationProvider {

    private final Yaml yaml;

    @Builder(setterPrefix = "set")
    public YamlConfigurationProvider(@NonNull ConfigurationSource source) {
        super(source);
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }

    @Override
    @SneakyThrows
    public void load() {
        File file = source.getFile();
        @Cleanup Reader reader = Files.newBufferedReader(file.toPath());
        entries = yaml.load(reader);
        if (entries == null) {
            entries = new HashMap<>();
        }
    }

    @Override
    @SneakyThrows
    public void update(Map<String, Property> properties) {
        load();
        Map<String, Object> output = new HashMap<>();
        boolean changedOutput = false;
        // Add all loaded entries
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            output.put((String) entry.getKey(), entry.getValue());
        }
        // Add all missing entries
        for (Map.Entry<String, Property> property : properties.entrySet()) {
            if (!output.containsKey(property.getKey())) {
                Object value = property.getValue().getValue();
                output.put(property.getKey(), value);
                changedOutput = true;
            }
        }
        // Remove all unnecessary entries
        for (Map.Entry<String, Object> entry : new ArrayList<>(output.entrySet())) {
            if (!properties.containsKey(entry.getKey())) {
                output.remove(entry.getKey());
                changedOutput = true;
            }
        }
        if (changedOutput) {
            PrintWriter writer = new PrintWriter(getSource().getFile());
            yaml.dump(output, writer);
            writer.close();
            load();
        }
    }
}
