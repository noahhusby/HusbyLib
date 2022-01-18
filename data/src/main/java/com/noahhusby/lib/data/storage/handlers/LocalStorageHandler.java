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

package com.noahhusby.lib.data.storage.handlers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.noahhusby.lib.data.storage.StorageActions;
import com.noahhusby.lib.data.storage.StorageUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LocalStorageHandler<T> extends StorageHandler<T> {
    private final File file;

    private final StorageActions<T> actions = new StorageActions<T>() {
        @Override
        public void add(T o) {

        }

        @Override
        public void remove(T o) {

        }

        @Override
        public void update(T o) {

        }

        @Override
        public Collection<T> get() {
            List<T> objects = new ArrayList<>();
            try {
                JsonArray array = StorageUtil.gson.fromJson(new FileReader(file), JsonArray.class);
                for (JsonElement element : array) {
                    objects.add(StorageUtil.gson.fromJson(element, storage.getClassType()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return objects;
        }
    };

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
    public void save() {
        try {
            FileWriter writer = new FileWriter(file);
            String json = StorageUtil.gson.toJson(storage.actions().get());
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        for (T obj : Lists.newArrayList(storage.actions().get())) {
            storage.actions().remove(obj);
        }
        for (T obj : actions.get()) {
            storage.actions().add(obj);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public StorageActions<T> actions() {
        return actions;
    }

    @Override
    public void close() {

    }
}
