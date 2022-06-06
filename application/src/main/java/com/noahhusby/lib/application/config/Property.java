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

import lombok.Value;

import java.lang.reflect.Field;

/**
 * A class representing a specific property in a config file
 *
 * @author Noah Husby
 */
@Value
public class Property {
    /**
     * Name of field, or other name if specified
     */
    String name;

    /**
     * Comment of property
     */
    String[] comment;

    /**
     * Default value specified by config file
     */
    Object value;

    /**
     * Field of property
     */
    Field field;

    /**
     * An overriding environment variable. Null if unset.
     */
    String environmentVariable;

    /**
     * Creates a configuration property from a field.
     *
     * @param clazz The class where the field is located.
     * @param field {@link Field} of a configuration class.
     * @return A {@link Property} representation of a field.
     * @throws IllegalAccessException if the field cannot be access in the given class.
     */
    static Property of(Object clazz, Field field) throws IllegalAccessException {
        Config.Name nameAnnotation = field.getAnnotation(Config.Name.class);
        Config.Comment commentAnnotation = field.getAnnotation(Config.Comment.class);
        String environmentVariable = field.isAnnotationPresent(EnvironmentVariable.class) ? field.getAnnotation(EnvironmentVariable.class).value() : null;
        String name = nameAnnotation == null ? field.getName() : nameAnnotation.value();
        String[] comment = commentAnnotation == null ? null : commentAnnotation.value();
        field.setAccessible(true);
        return new Property(name, comment, field.get(clazz), field, environmentVariable);
    }
}
