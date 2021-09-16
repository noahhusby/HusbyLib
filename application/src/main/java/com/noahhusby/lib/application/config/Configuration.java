package com.noahhusby.lib.application.config;

import com.noahhusby.lib.application.config.provider.ConfigurationProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
@Getter
public class Configuration {
    private final ConfigurationProvider provider;

    public void load() {
        provider.load();
    }

    public <T> T bind(Class<T> clazz) {
        return null;
    }

    @SneakyThrows
    public void sync(Class<?> clazz) {
        provider.update(ConfigHandler.getProperties(clazz));
    }
}
