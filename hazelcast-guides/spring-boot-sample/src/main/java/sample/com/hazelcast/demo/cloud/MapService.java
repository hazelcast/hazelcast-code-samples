package sample.com.hazelcast.demo.cloud;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

// tag::class[]
@Component
public class MapService {

    private static final Logger logger = LoggerFactory.getLogger(MapService.class);

    private final HazelcastInstance hazelcastInstance;

    public MapService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @EventListener
    public void onApplicationIsReady(ContextRefreshedEvent contextRefreshedEvent) {
        var mapName = "MyMap";
        Map<String, String> myMap = hazelcastInstance.getMap(mapName);
        for (int i = 0; i < 10; i++) {
            myMap.put(UUID.randomUUID().toString(), "Value-" + i);
        }
        logger.info("Map prepopulated [mapName={},size={}]", mapName, myMap.size());
    }

}
// end::class[]