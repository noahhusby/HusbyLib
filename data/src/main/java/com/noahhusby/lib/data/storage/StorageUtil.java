package com.noahhusby.lib.data.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * @author Noah Husby
 */
@UtilityClass
public class StorageUtil {

    protected static final Gson excludedGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .create();

    protected static final Gson gson = new Gson();


    protected static String getKeyAnnotation(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return null;
        }
        if (clazz.isAnnotationPresent(Key.class)) {
            return clazz.getAnnotation(Key.class).value();
        }
        return null;
    }
}
