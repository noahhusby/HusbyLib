package com.noahhusby.lib.data.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;
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

    public static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                if(src == src.intValue()) {
                    return new JsonPrimitive(src.intValue());
                } else if(src == src.longValue()) {
                    return new JsonPrimitive(src.longValue());
                } else if(src == src.floatValue()) {
                    return new JsonPrimitive(src.floatValue());
                }
                return new JsonPrimitive(src);
            })
            .create();


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
