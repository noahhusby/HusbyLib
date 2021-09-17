package com.noahhusby.lib.data.storage.handlers;

import com.google.gson.JsonArray;
import com.noahhusby.lib.data.storage.Storage;
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.compare.CompareResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LocalStorageHandler implements StorageHandler {
    private final File file;
    private int priority = 0;

    private Storage storage;

    public LocalStorageHandler(File file) {
        this.file = file;
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("[]");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void save(CompareResult result) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(result.getRawOutput().toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonArray load() {
        try {
            return StorageUtil.gson.fromJson(new FileReader(file), JsonArray.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JsonArray();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {

    }
}
