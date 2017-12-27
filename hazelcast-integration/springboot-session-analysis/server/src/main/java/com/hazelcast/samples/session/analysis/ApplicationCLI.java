package com.hazelcast.samples.session.analysis;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;

/**
 * <p>Additional commands for this application to augment those
 * provided by default by <a href="https://projects.spring.io/spring-shell/">Spring Shell</a>
 * </p>
 * <p>Our commands are in upper case.
 * </p>
 */
@Component
public class ApplicationCLI implements CommandMarker {

    private HazelcastInstance hazelcastInstance;
    private JetInstance jetInstance;

    /**
     * <p>Spring will inject the {@code @Bean} objects in to the
     * constructor. Use this as a convenient point to initialise
     * maps rather than let this occur on first use.
     * </p>
     *
     * @param hazelcastInstance Spring {@code @Bean}
     * @param jetInstance Spring {@code @Bean}
     */
    public ApplicationCLI(HazelcastInstance hazelcastInstance, JetInstance jetInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.jetInstance = jetInstance;

        for (String iMapName : Constants.IMAP_NAMES) {
            this.hazelcastInstance.getMap(iMapName);
        }
    }

    /**
     * <p>List IMaps in name order, and the keys contained in each (but not the values).
     * </p>
     * <p>Note this is essentially a {@link com.hazelcast.core.IMap#get IMap#get} or
     * <b><i>read</i></b> operation. It won't interfere with session expiry as this
     * is set on <b><i>write</i></b>.
     * </p>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @CliCommand(value = "LIST", help = "List map keys")
    public void listIMaps() {
        Set<String> iMapNames = this.hazelcastInstance.getDistributedObjects().stream()
                .filter(distributedObject -> distributedObject instanceof IMap)
                .filter(distributedObject -> !distributedObject.getName().startsWith(Jet.INTERNAL_JET_OBJECTS_PREFIX))
                .map(distributedObject -> distributedObject.getName()).collect(Collectors.toCollection(TreeSet::new));

        iMapNames.stream().forEach(name -> {
            IMap<?, ?> iMap = this.hazelcastInstance.getMap(name);

            System.out.printf("IMap: '%s'%n", name);

            // Sort if possible
            Set<?> keys = iMap.keySet();
            if (!keys.isEmpty() && keys.iterator().next() instanceof Comparable) {
                keys = new TreeSet(keys);
            }

            keys.stream().forEach(key -> {
                System.out.printf("    -> '%s'%n", key);
            });

            System.out.printf("[%d entr%s]%n", iMap.size(), (iMap.size() == 1 ? "y" : "ies"));
        });
    }

}
