package com.hazelcast.examples;

import com.hazelcast.config.Config;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NearCacheWithSerializedKeys extends NearCacheSupport {

    public static void main(String[] args) {
        HazelcastInstance hz = initCluster();
        IMap<ArticleKey, Article> map1 = hz.getMap("articlesObject");
        IMap<ArticleKey, Article> map2 = hz.getMap("articlesSerializedKeys");

        Config config = hz.getConfig();
        NearCacheConfig nearCacheConfig1 = config.getMapConfig("articlesObject").getNearCacheConfig();
        NearCacheConfig nearCacheConfig2 = config.getMapConfig("articlesSerializedKeys").getNearCacheConfig();

        System.out.println("Near Cache of map 1 uses serialized keys: " + nearCacheConfig1.isSerializeKeys());
        System.out.println("Near Cache of map 2 uses serialized keys: " + nearCacheConfig2.isSerializeKeys());

        ArticleKey key1 = new ArticleKey(1);
        ArticleKey key2 = new ArticleKey(2);
        Article article = new Article("foo");

        System.out.println("\nPopulate the data structure (both keys are serialized, since it's a remote operation)...");
        map1.put(key1, article);
        map2.put(key2, article);

        System.out.println("\nThe first get() will populate the Near Cache (again a remote operation for both keys)...");
        map1.get(key1);
        map2.get(key2);

        System.out.println("\nThe second get() will be served from the Near Cache (key 2 is serialized for the local lookup)...");
        map1.get(key1);
        map2.get(key2);

        System.out.println("\nThe Near Cache statistics are the same (the key serialization is transparent to the user)...");
        System.out.println("Near Cache from map 1: " + map1.getLocalMapStats().getNearCacheStats());
        System.out.println("Near Cache from map 2: " + map2.getLocalMapStats().getNearCacheStats());

        shutdown();
    }
}
