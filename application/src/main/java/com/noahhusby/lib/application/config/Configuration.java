package com.noahhusby.lib.application.config;

import com.noahhusby.lib.application.config.provider.ConfigurationProvider;
import com.noahhusby.lib.common.util.HusbyUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
        Map<String, Property> properties = ConfigHandler.getProperties(clazz);
        provider.update(ConfigHandler.getProperties(clazz));
        for(Property property : properties.values()) {
            Type type = property.getType();
            Object object = HusbyUtil.GSON.fromJson(HusbyUtil.GSON.toJson(provider.getEntries().get(property.getName())), type);
            Field field = property.getField();
            field.setAccessible(true);
            field.set(clazz.newInstance(), object);
        }
    }
}
