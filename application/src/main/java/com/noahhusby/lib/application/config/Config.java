package com.noahhusby.lib.application.config;

import com.noahhusby.lib.application.config.provider.ConfigurationProvider;
import com.noahhusby.lib.application.config.provider.JsonConfigurationProvider;
import com.noahhusby.lib.application.config.provider.VisualConfigurationProvider;
import com.noahhusby.lib.application.config.provider.YamlConfigurationProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    /**
     * Name of the config file
     * "config" is the default name
     */
    String name() default "config";

    /**
     * The type of config file
     * {@link Type}
     */
    Type type() default Type.JSON;

    @AllArgsConstructor
    @Getter
    enum Type {
        JSON(".json", JsonConfigurationProvider.class),
        YAML(".yml", YamlConfigurationProvider.class),
        VISUAL(".cfg", VisualConfigurationProvider.class);

        private String extension;
        private Class<? extends ConfigurationProvider> provider;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Comment {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {
        String value();
    }
}
