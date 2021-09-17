package com.noahhusby.lib.application.config;

import com.google.gson.JsonElement;
import com.noahhusby.lib.application.config.provider.ConfigurationProvider;
import com.noahhusby.lib.common.util.HusbyUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
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

    @SneakyThrows
    public <T> T bind(Class<T> clazz) {
        provider.update(ConfigHandler.getProperties(clazz));
        JsonElement asElement = HusbyUtil.GSON.toJsonTree(provider.getEntries());
        return HusbyUtil.GSON.fromJson(asElement, (Type) clazz);
    }

    @SneakyThrows
    public void sync(Class<?> clazz) {
        Map<String, Property> properties = ConfigHandler.getProperties(clazz);
        provider.update(ConfigHandler.getProperties(clazz));
        for (Property property : properties.values()) {
            Type type = property.getType();
            Object object = HusbyUtil.GSON.fromJson(HusbyUtil.GSON.toJson(provider.getEntries().get(property.getName())), type);
            Field field = property.getField();
            field.setAccessible(true);
            field.set(clazz.newInstance(), object);
        }
    }

    public boolean has(@NonNull String key) {
        return provider.getEntries().containsKey(key);
    }

    public Object get(@NonNull String key) {
        return provider.getEntries().get(key);
    }

    public Object getOrDefault(@NonNull String key, Object def) {
        Object object = get(key);
        return object == null ? def : object;
    }

    public String getAsString(String key) {
        return (String) get(key);
    }

    public String[] getAsStringList(String key) {
        return (String[]) get(key);
    }

    public int getAsInteger(String key) {
        return (int) get(key);
    }

    public boolean getAsBoolean(String key) {
        return (boolean) get(key);
    }
}
