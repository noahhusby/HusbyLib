package com.noahhusby.lib.application.config.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

/**
 * @author Noah Husby
 */
@RequiredArgsConstructor
public class FileConfigurationSource implements ConfigurationSource {

    @Getter
    private final File file;

    @Override
    public boolean init() {
        try {
            if(file == null) {
                return false;
            }
            if(file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            if(!file.exists()) {
                if(!file.createNewFile()) {
                    return false;
                }
            }
        } catch (IOException e) {
            //TODO: LOG
            return false;
        }
        return file.canRead() && file.canWrite();
    }
}
