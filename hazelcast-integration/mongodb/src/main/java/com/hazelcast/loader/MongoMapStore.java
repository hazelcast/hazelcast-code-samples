/*
 *
 *  Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.hazelcast.loader;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * MapStore implementation for MongoDB
 *
 * @author Viktor Gamov on 11/4/15.
 *         Twitter: @gamussa
 */
public class MongoMapStore implements MapStore<String, Supplement>, MapLoaderLifecycleSupport {
    private MongoClient mongoClient;
    private MongoCollection collection;


    public MongoMapStore() {
    }


    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        // null check
        final String mongoUrl = (String) properties.get("mongo.url");
        final String dbName = (String) properties.get("mongo.db");
        final String collectionName = (String) properties.get("mongo.collection");
        this.mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
        this.collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
    }


    @Override
    public void destroy() {
        mongoClient.close();
    }

    @Override public Supplement load(String key) {
        System.out.println("Load " + key);
        final Document document =
            (Document) collection.find(eq("_id", key)).first();
        final String name = (String) document.get("name");
        final Integer price = document.getInteger("price");
        return new Supplement(name, price);

    }

    @Override public Map loadAll(Collection keys) {
        System.out.println("LoadAll " + keys);
        final HashMap<String, Supplement> result = new HashMap<String, Supplement>();

        final FindIterable<Document> id = collection.find(in("_id", keys));
        for (Document document : id) {
            final String name = (String) document.get("name");
            final Integer price = document.getInteger("price");
            result.put(document.get("_id").toString(), new Supplement(name, price));
        }
        return result;
    }

    @Override public Iterable loadAllKeys() {
        System.out.println("LoadAllKeys");
        List<String> keys = new LinkedList<String>();
        final FindIterable<Document> ids = collection.find().projection(Projections.include("_id"));
        for (Document document : ids) {
            keys.add(document.get("_id").toString());
        }
        return keys;
    }

    @Override
    public void store(String key, Supplement value) {
        Document doc = new Document("name", value.getName()).append("price", value.getPrice())
            .append("_id", key);
        this.collection.insertOne(doc);
        //this.collection.replaceOne(doc, doc);
    }

    @Override
    public void storeAll(Map<String, Supplement> map) {
        final List<InsertOneModel> batch = new LinkedList<InsertOneModel>();
        for (Map.Entry<String, Supplement> entry : map.entrySet()) {
            final String k = entry.getKey();
            final Supplement v = entry.getValue();
            batch.add(new InsertOneModel(
                new Document("name", v.getName()).append("price", v.getPrice())
                    .append("_id", k)));
        }

        this.collection.bulkWrite(batch, new BulkWriteOptions().ordered(false));

    }

    @Override
    public void delete(String key) {
        this.collection.deleteOne(eq("_id", key));

    }

    @Override
    public void deleteAll(Collection<String> keys) {
        this.collection.deleteMany(in("_id", keys));
    }
}
