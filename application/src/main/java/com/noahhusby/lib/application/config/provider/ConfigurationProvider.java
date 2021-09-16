package com.noahhusby.lib.application.config.provider;

import com.noahhusby.lib.application.config.Property;
import com.noahhusby.lib.application.config.source.ConfigurationSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ConfigurationProvider {
    @Getter
    protected final ConfigurationSource source;

    @Getter
    protected Map<?, ?> entries = new HashMap<>();

    public ConfigurationProvider(ConfigurationSource source) {
        this.source = source;
        source.init();
    }

    public abstract void load();

    public abstract void update(Map<String, Property> properties);
}
