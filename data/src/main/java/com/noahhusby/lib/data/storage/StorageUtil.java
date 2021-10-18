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

package com.noahhusby.lib.data.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * @author Noah Husby
 */
@UtilityClass
public class StorageUtil {

    public static final Gson excludedGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .create();

    public static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setLenient()
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                if (src == src.intValue()) {
                    return new JsonPrimitive(src.intValue());
                } else if (src == src.longValue()) {
                    return new JsonPrimitive(src.longValue());
                } else if (src == src.floatValue()) {
                    return new JsonPrimitive(src.floatValue());
                }
                return new JsonPrimitive(src);
            })
            .create();


    public static String getKeyAnnotation(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return null;
        }
        if (clazz.isAnnotationPresent(Key.class)) {
            return clazz.getAnnotation(Key.class).value();
        }
        return null;
    }
}
