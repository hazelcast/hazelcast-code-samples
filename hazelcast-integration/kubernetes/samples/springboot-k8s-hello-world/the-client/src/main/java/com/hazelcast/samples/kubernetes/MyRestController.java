package com.hazelcast.samples.kubernetes;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * This is the "business logic" of the application. Return a collection
 * of strings in response to a REST call.
 * </p>
 * <p>
 * You can test this with:
 * <pre>
 * curl -v http://localhost:8082
 * </pre>
 * substituting the port you've selected if not 8082.
 * </p>
 */
@RestController
public class MyRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyRestController.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @GetMapping
    public Collection<String> index() {
        LOGGER.info("index()");

        IMap<String, String> helloMap = this.hazelcastInstance.getMap("hello");

        Set<String> keys = new TreeSet<>(helloMap.keySet());

        Collection<String> result = new ArrayList<>();

        for (String key : keys) {
            result.add(helloMap.get(key));
        }

        return result;
    }
}
