package com.noahhusby.lib.application.config.source;

import java.io.File;

/**
 * @author Noah Husby
 */
public interface ConfigurationSource {
    File getFile();

    boolean init();
}
