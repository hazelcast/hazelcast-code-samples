package sample.com.hz.demos.mapstore.mongo;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStore;

/*
* Snippets of this code are included as examples in our documentation,
* using the tag:: comments.
*/

@Slf4j
// tag::class[]
public class MongoPersonMapStore implements MapStore<Integer, Person>, MapLoaderLifecycleSupport {

    private MongoClient mongoClient;

    private PersonRepository personRepository;
    
    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        this.mongoClient = new MongoClient(new MongoClientURI(properties.getProperty("uri")));
        MongoDatabase database = this.mongoClient.getDatabase(properties.getProperty("database"));
        this.personRepository = new MongoPersonRepository(mapName, database);
        log.info("MongoPersonMapStore::initialized");
    }

    @Override
    public void destroy() {
        MongoClient mongoClient = this.mongoClient;
        if (mongoClient != null) {
            mongoClient.close();
        }
        log.info("MongoPersonMapStore::destroyed");
    }

    @Override
    public void store(Integer key, Person value) {
        log.info("MongoPersonMapStore::store key {} value {}", key, value);
        getRepository().save(Person.builder()
            .id(key)
            .name(value.getName())
            .lastname(value.getLastname())
            .build());
    }

    @Override
    public void storeAll(Map<Integer, Person> map) {
        log.info("MongoPersonMapStore::store all {}", map);
        for (Map.Entry<Integer, Person> entry : map.entrySet()) {
            store(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void delete(Integer key) {
        log.info("MongoPersonMapStore::delete key {}", key);
        getRepository().delete(key);
    }

    @Override
    public void deleteAll(Collection<Integer> keys) {
        log.info("MongoPersonMapStore::delete all {}", keys);
        getRepository().delete(keys);
    }

    @Override
    public Person load(Integer key) {
        log.info("MongoPersonMapStore::load by key {}", key);
        return getRepository().find(key).orElse(null);
    }

    @Override
    public Map<Integer, Person> loadAll(Collection<Integer> keys) {
        log.info("MongoPersonMapStore::loadAll by keys {}", keys);
        return getRepository().findAll(keys).stream()
            .collect(Collectors.toMap(Person::getId, Function.identity()));
    }

    @Override
    public Iterable<Integer> loadAllKeys() {
        log.info("MongoPersonMapStore::loadAllKeys");
        return getRepository().findAllIds();
    }

    private PersonRepository getRepository() {
        PersonRepository personRepository = this.personRepository;
        if (personRepository == null) {
            throw new IllegalStateException("Person Repository must not be null!");
        }
        return this.personRepository;
    }

}
// end::class[]
