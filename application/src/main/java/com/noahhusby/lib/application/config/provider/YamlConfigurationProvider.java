package com.noahhusby.lib.application.config.provider;

import com.noahhusby.lib.application.config.Property;
import com.noahhusby.lib.application.config.source.ConfigurationSource;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;

/**
 * @author Noah Husby
 */
public class YamlConfigurationProvider extends ConfigurationProvider {

    @Builder(setterPrefix = "set")
    public YamlConfigurationProvider(@NonNull ConfigurationSource source) {
        super(source);
    }

    @Override
    public void load() {

    }

    @Override
    public void update(Map<String, Property> properties) {

    }
}
