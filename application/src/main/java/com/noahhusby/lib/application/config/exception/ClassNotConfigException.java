package com.noahhusby.lib.application.config.exception;

/**
 * @author Noah Husby
 */
public class ClassNotConfigException extends Exception {
    public ClassNotConfigException() {
        super("Class does not implement \"@Config\" annotation.");
    }
}