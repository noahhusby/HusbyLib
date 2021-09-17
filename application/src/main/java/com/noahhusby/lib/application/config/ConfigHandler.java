package com.noahhusby.lib.application.config;

import com.google.gson.Gson;
import com.noahhusby.lib.application.config.exception.ClassNotConfigException;
import com.noahhusby.lib.application.config.provider.ConfigurationProvider;
import com.noahhusby.lib.application.config.source.FileConfigurationSource;
import lombok.NonNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Noah Husby
 */
public final class ConfigHandler {
    public static Gson gson = new Gson();

    public static Configuration of(@NonNull Class<?> clazz) throws ClassNotConfigException {
        return of(clazz, new File(System.getProperty("user.dir")));
    }

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
