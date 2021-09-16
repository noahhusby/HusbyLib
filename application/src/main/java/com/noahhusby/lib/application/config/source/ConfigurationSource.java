package com.noahhusby.lib.application.config.source;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Noah Husby
 */
public interface ConfigurationSource {
    File getFile();

    boolean init();
}
