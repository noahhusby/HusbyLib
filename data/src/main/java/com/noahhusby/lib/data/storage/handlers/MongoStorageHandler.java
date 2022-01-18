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
import com.noahhusby.lib.data.storage.StorageUtil;
import com.noahhusby.lib.data.storage.compare.ComparatorAction;
import com.noahhusby.lib.data.storage.compare.CompareResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class MongoStorageHandler<T> extends StorageHandler<T> {
    @Getter
    private final MongoCollection<Document> collection;

    @Override
    public void save(CompareResult result) {
        try {
            if (result.isCleared()) {
                collection.deleteMany(new Document());
            }

            for (Map.Entry<JsonObject, ComparatorAction> r : result.getComparedOutput().entrySet()) {
                JsonObject object = r.getKey();
                if (r.getValue() == ComparatorAction.REMOVE) {
                    Document document = Document.parse(StorageUtil.gson.toJson(object));
                    collection.deleteOne(new Document("_id", document.get(result.getKey())));
                }

                if (r.getValue() == ComparatorAction.ADD) {
                    Document document = Document.parse(StorageUtil.gson.toJson(object));
                    if (result.getKey() != null) {
                        document.append("_id", document.get(result.getKey()));
                    }
                    collection.insertOne(document);
                }

                if (r.getValue() == ComparatorAction.UPDATE) {
                    Document document = Document.parse(StorageUtil.gson.toJson(object));
                    collection.replaceOne(new Document("_id", document.get(result.getKey())), document);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonArray load() {
        FindIterable<Document> documents = collection.find();
        JsonArray array = new JsonArray();
        for (Document document : documents) {
            array.add(StorageUtil.gson.toJsonTree(document));
        }
        return array;
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
    public void close() {
    }
}
