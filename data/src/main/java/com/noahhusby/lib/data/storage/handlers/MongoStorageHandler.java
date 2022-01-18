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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.noahhusby.lib.data.storage.StorageActions;
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class MongoStorageHandler<T> extends StorageHandler<T> {
    @Getter
    private final MongoCollection<Document> collection;

    private final StorageActions<T> actions = new StorageActions<T>() {
        @Override
        public void add(T o) {
            Document document = Document.parse(StorageUtil.gson.toJson(o));
            document.append("_id", document.get(storage.getKey()));
            collection.insertOne(document);
        }

        @Override
        public void remove(T o) {
            Document document = Document.parse(StorageUtil.gson.toJson(o));
            collection.deleteOne(new Document("_id", document.get(storage.getKey())));
        }

        @Override
        public void update(T o) {
            Document document = Document.parse(StorageUtil.gson.toJson(o));
            collection.replaceOne(new Document("_id", document.get(storage.getKey())), document);
        }

        @Override
        public Collection<T> get() {
            FindIterable<Document> documents = collection.find();
            ArrayList<T> objects = new ArrayList<>();
            for(Document document : documents) {
                T object = StorageUtil.gson.fromJson(StorageUtil.gson.toJsonTree(document), storage.getClassType());
                objects.add(object);
            }
            return objects;
        }
    };

    @Override
    public void save() {
        comparator.save(actions);
    }

    @Override
    public void load() {
        comparator.load(storage.actions());
    }

    public void enableEventUpdates() {
        collection.watch().forEach((Consumer<? super ChangeStreamDocument<Document>>) x -> {
            if (x.getOperationType() == OperationType.INSERT || x.getOperationType() == OperationType.REPLACE || x.getOperationType() == OperationType.DELETE) {
                load();
            }
        });
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
