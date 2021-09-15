package com.noahhusby.lib.application.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    String name() default "config";


    public static enum Type {
        private String extension;
        private ConfigFileHandler handler;
    }
}
