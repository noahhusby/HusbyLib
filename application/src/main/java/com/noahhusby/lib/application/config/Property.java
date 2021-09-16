package com.noahhusby.lib.application.config;

import lombok.Data;
import lombok.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author Noah Husby
 */
@Value
public class Property {
    String name;
    String[] comment;
    Object value;
    Type type;
    Field field;
}
