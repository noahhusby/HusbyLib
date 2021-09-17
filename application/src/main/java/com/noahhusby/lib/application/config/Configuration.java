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

package com.noahhusby.lib.application.config;

import com.google.gson.JsonElement;
import com.noahhusby.lib.application.config.exception.ClassNotConfigException;
import com.noahhusby.lib.application.config.provider.ConfigurationProvider;
import com.noahhusby.lib.application.config.source.FileConfigurationSource;
import com.noahhusby.lib.common.util.HusbyUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
@Getter
public class Configuration {
    private final ConfigurationProvider provider;

    /**
     * Loads the config file data
     */
    public void load() {
        provider.load();
    }

    /**
     * Gets a config class object from the config file.
     *
     * @param clazz Config class.
     * @param <T> Config class type.
     * @return T class
     */
    @SneakyThrows
    public <T> T bind(Class<T> clazz) {
        provider.update(getProperties(clazz));
        JsonElement asElement = HusbyUtil.GSON.toJsonTree(provider.getEntries());
        return HusbyUtil.GSON.fromJson(asElement, (Type) clazz);
    }

    /**
     * Updates config file from class, and updates config instance with values from config.
     *
     * @param clazz Config class.
     */
    @SneakyThrows
    public void sync(Class<?> clazz) {
        Map<String, Property> properties = getProperties(clazz);
        provider.update(getProperties(clazz));
        for (Property property : properties.values()) {
            Type type = property.getType();
            Object object = HusbyUtil.GSON.fromJson(HusbyUtil.GSON.toJson(provider.getEntries().get(property.getName())), type);
            Field field = property.getField();
            field.setAccessible(true);
            field.set(clazz.newInstance(), object);
        }
    }

    /**
     * Checks if the config contains the specified member.
     *
     * @param key Key of member.
     * @return True if the member exists, false if not.
     */
    public boolean has(@NonNull String key) {
        return provider.getEntries().containsKey(key);
    }

    /**
     * Get the specified member.
     *
     * @param key Key of member.
     * @return the object correlating to the specified member.
     */
    public Object get(@NonNull String key) {
        return provider.getEntries().get(key);
    }

    /**
     * Get the specified member or a default value if the specific member does not exist.
     *
     * @param key Key of member.
     * @param def Default object if member doesn't exist.
     * @return the object correlating to the specified member or a default value if the specific member does not exist.
     */
    public Object getOrDefault(@NonNull String key, Object def) {
        Object object = get(key);
        return object == null ? def : object;
    }

    /**
     * Get the specified member as a string.
     *
     * @param key Key of member.
     * @return the string correlating to the specified member.
     */
    public String getAsString(String key) {
        return (String) get(key);
    }

    /**
     * Get the specified member as a string array.
     *
     * @param key Key of member.
     * @return the string array correlating to the specified member.
     */
    public String[] getAsStringList(String key) {
        return (String[]) get(key);
    }

    /**
     * Get the specified member as an integer.
     *
     * @param key Key of member.
     * @return the integer correlating to the specified member.
     */
    public int getAsInteger(String key) {
        return (int) get(key);
    }

    /**
     * Get the specified member as a boolean.
     *
     * @param key Key of member.
     * @return the boolean correlating to the specified member.
     */
    public boolean getAsBoolean(String key) {
        return (boolean) get(key);
    }

    /**
     * Create a configuration instance from a class with the {@link Config} annotation.
     *
     * @param clazz Class with a {@link Config} annotation.
     * @return {@link Configuration}.
     * @throws ClassNotConfigException if the class does not have the {@link Config} annotation.
     */
    public static Configuration of(@NonNull Class<?> clazz) throws ClassNotConfigException {
        return of(clazz, new File(System.getProperty("user.dir")));
    }

    /**
     * Create a configuration instance from a class with the {@link Config} annotation.
     *
     * @param clazz Class with a {@link Config} annotation.
     * @param directory A custom directory for the file.
     * @return {@link Configuration} instance.
     * @throws ClassNotConfigException if the class does not have the {@link Config} annotation.
     */
    public static Configuration of(@NonNull Class<?> clazz, @NonNull File directory) throws ClassNotConfigException {
        if (!clazz.isAnnotationPresent(Config.class)) {
            throw new ClassNotConfigException();
        }

        Config config = clazz.getAnnotation(Config.class);
        String fileName = config.name().contains(".") ? config.name() : config.name() + config.type().getExtension();
        FileConfigurationSource fileSource = new FileConfigurationSource(new File(directory, fileName));
        Class<? extends ConfigurationProvider> configurationProviderClass = config.type().getProvider();
        try {
            ConfigurationProvider provider = (ConfigurationProvider) configurationProviderClass.getConstructors()[0].newInstance(fileSource);
            return new Configuration(provider);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get a map of properties from a config class.
     *
     * @param clazz Class with a {@link Config} annotation.
     * @return A map of properties
     * @throws IllegalAccessException if a given property cannot be accessed
     */
    public static Map<String, Property> getProperties(Class<?> clazz) throws IllegalAccessException {
        Map<String, Property> properties = new HashMap<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(Config.Ignore.class)) {
                continue;
            }
            Config.Name nameAnnotation = field.getAnnotation(Config.Name.class);
            Config.Comment commentAnnotation = field.getAnnotation(Config.Comment.class);
            String name = nameAnnotation == null ? field.getName() : nameAnnotation.value();
            String[] comment = commentAnnotation == null ? null : commentAnnotation.value();
            properties.put(name, new Property(name, comment, field.get(clazz), field.getType(), field));
        }
        return properties;
    }
}
