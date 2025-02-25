package guides.hazelcast.micronaut;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.CacheInvalidate;
import io.micronaut.cache.annotation.CachePut;
import io.micronaut.cache.annotation.Cacheable;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@CacheConfig(cacheNames = {"entries"})
public class CommandService {
    private Map<String, String> entries = new ConcurrentHashMap<>();

    @CachePut(parameters = "key")
    public String put(String key, String value) {
        entries.put(key, value);
        return value;
    }

    @Cacheable
    public String get(String key) {
        return entries.get(key);
    }

    @CacheInvalidate(all = true)
    public void reset() {
        entries.clear();
    }

    @CacheInvalidate()
    public void reset(String key) {
        entries.remove(key);
    }

    @CacheInvalidate
    public void set(String key, String value) {
        entries.put(key, value);
    }
}
