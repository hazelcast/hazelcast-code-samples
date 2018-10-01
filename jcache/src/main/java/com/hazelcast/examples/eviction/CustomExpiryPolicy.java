/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.examples.eviction;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;

import static java.util.concurrent.TimeUnit.SECONDS;

public class CustomExpiryPolicy {
    static {
        System.setProperty("hazelcast.jcache.provider.type", "server");
    }

    public static void main(String[] args) throws InterruptedException {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        CacheConfig cacheConfig = createCacheConfig("myCache", true);
        ICache<Integer, String> cache = (ICache<Integer, String>) cacheManager.createCache("myCache", cacheConfig);

        cache.put(1, "Value");
        Thread.sleep(SECONDS.toMillis(2));
        // the entry is automatically removed
        System.out.println("Entry value: " + cache.get(1));

        cache.put(1, "Value");
        // access operation does not change expiry time, so the entry will be removed after 2 seconds.
        cache.get(1);
        Thread.sleep(SECONDS.toMillis(2));
        System.out.println("Entry value: " + cache.get(1));

        cache.put(1, "Value");
        // update operation makes the entry eternal
        cache.put(1, "NewValue");
        Thread.sleep(SECONDS.toMillis(2));
        System.out.println("Entry value: " + cache.get(1));


        cache.put(2, "Value", new ShortExpiryPolicy());
        Thread.sleep(SECONDS.toMillis(1));
        // ShortExpiryPolicy overrides this cache's expiry policy and causes the entry to be removed after 1 second
        System.out.println("Entry value: " + cache.get(2));

        cachingProvider.close();
    }

    private static CacheConfig createCacheConfig(String cacheName, boolean useExpiryPolicyFactory) {
        CacheConfig config = new CacheConfig().setName(cacheName);
        if (useExpiryPolicyFactory) {
            config.setExpiryPolicyFactory(new Factory<ExpiryPolicy>() {
                @Override
                public ExpiryPolicy create() {
                    return new MyExpiryPolicy();
                }
            });
        }
        return config;
    }

    static class MyExpiryPolicy implements ExpiryPolicy {

        /**
         * By default, new entries live for only two seconds
         * @return
         */
        @Override
        public Duration getExpiryForCreation() {
            return new Duration(SECONDS, 2);
        }

        /**
         * Access operations do not change expiry time
         * @return
         */
        @Override
        public Duration getExpiryForAccess() {
            return null;
        }

        /**
         * Update operations make the entry eternal
         * @return
         */
        @Override
        public Duration getExpiryForUpdate() {
            return Duration.ETERNAL;
        }
    }

    static class ShortExpiryPolicy implements ExpiryPolicy {

        @Override
        public Duration getExpiryForCreation() {
            return new Duration(SECONDS, 1);
        }

        @Override
        public Duration getExpiryForAccess() {
            return new Duration(SECONDS, 1);
        }

        @Override
        public Duration getExpiryForUpdate() {
            return new Duration(SECONDS, 1);
        }
    }
}
