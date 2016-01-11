package com.hazelcast.examples.jcache.client;

import com.hazelcast.examples.AbstractApp;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import java.io.IOException;

public class PortableDomainObjectExample extends AbstractApp {

    private static final String KEY = "foo";

    private void runApp() {
        ClusterGroup server = startServer();
        clientSetup();

        CacheManager cacheManager = initCacheManager(uri1);
        Cache<String, DomainObject> cache = getCache(cacheManager);

        DomainObject originalObject = new DomainObject("foo", 0);
        cache.put(KEY, originalObject);
        DomainObject objectFromCache = cache.get(KEY);

        System.out.println(originalObject.equals(objectFromCache));

        cleanUp(server, cacheManager);
    }

    private void cleanUp(ClusterGroup server, CacheManager cacheManager1) {
        cacheManager1.close();
        server.shutdown();
    }

    private Cache<String, DomainObject> getCache(CacheManager cacheManager1) {
        MutableConfiguration<String, DomainObject> config = new MutableConfiguration<String, DomainObject>();
        config.setStoreByValue(true)
                .setTypes(String.class, DomainObject.class)
                .setStatisticsEnabled(false);

        return cacheManager1.createCache("cache", config);
    }

    private ClusterGroup startServer() {
        ClusterGroup server = new ClusterGroup();
        server.init();
        return server;
    }

    public static void main(String[] args) {
        new PortableDomainObjectExample().runApp();
    }

    private static final class DomainObject implements Portable {

        private String name;
        private long id;

        DomainObject(String name, long id) {
            this.name = name;
            this.id = id;
        }

        private DomainObject() {
            // used by DomainObjectPortableFactory only
        }

        @Override
        public int getFactoryId() {
            return DomainObjectPortableFactory.FACTORY_ID;
        }

        @Override
        public int getClassId() {
            return DomainObjectPortableFactory.DOMAIN_CLASS_ID;
        }

        @Override
        public void writePortable(PortableWriter portableWriter) throws IOException {
            System.out.println("Writing portable");
            portableWriter.writeUTF("name", name);
            portableWriter.writeLong("id", id);
        }

        @Override
        public void readPortable(PortableReader portableReader) throws IOException {
            System.out.println("Reading portable");
            name = portableReader.readUTF("name");
            id = portableReader.readLong("id");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DomainObject that = (DomainObject) o;

            if (id != that.id) {
                return false;
            }
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (int) (id ^ (id >>> 32));
            return result;
        }
    }

    public static class DomainObjectPortableFactory implements PortableFactory {

        static final int DOMAIN_CLASS_ID = 1;
        static final int FACTORY_ID = 1;

        @Override
        public Portable create(int i) {
            switch (i) {
                case DOMAIN_CLASS_ID:
                    return new DomainObject();
                default:
                    throw new IllegalArgumentException("Factory " + getClass().getName() + " doesn't know objectId " + i);
            }
        }
    }
}
