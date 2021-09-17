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

import com.noahhusby.lib.data.storage.compare.Comparator;
import com.noahhusby.lib.data.storage.handlers.StorageHandler;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface Storage extends Closeable {
    void registerHandler(StorageHandler handler);

    void registerHandler(StorageHandler handler, Comparator comparator);

    Map<StorageHandler, Comparator> getHandlers();

    void unregisterHandler(StorageHandler handler);

    void clearHandlers();

    void load();

    void loadAsync();

    void save();

    void saveAsync();

    void setAutoLoad(long period, TimeUnit unit);

    void setAutoSave(long period, TimeUnit unit);

    void migrate();

    void migrate(MigrateMode mode);

    void migrate(int priority);

    void onLoadEvent(Runnable runnable);

    void onSaveEvent(Runnable runnable);
}
