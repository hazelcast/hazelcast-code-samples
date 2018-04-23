package com.hazelcast.samples.session.analysis;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.core.JobStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Additional commands for this application to augment those
 * provided by default by <a href="https://projects.spring.io/spring-shell/">Spring Shell</a>
 * </p>
 * <p>Our commands are in upper case.
 * </p>
 */
@ShellComponent
@Slf4j
public class ApplicationCLI {

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

        // Initialise all maps
        for (String iMapName : Constants.IMAP_NAMES) {
            this.hazelcastInstance.getMap(iMapName);
        }

        // Populate maps if needed, first member in cluster
        IMap<String, Integer> stockMap = this.hazelcastInstance.getMap(Constants.IMAP_NAME_STOCK);
        if (stockMap.isEmpty()) {
                for (Object[] item : Constants.TESTDATA) {
                    String key = item[0].toString();
                    Integer value = Integer.valueOf(item[1].toString());
                    stockMap.set(key, value);
                }
                log.info("Loaded {} into IMap '{}'", Constants.TESTDATA.length, stockMap.getName());
        }

    }

    /**
     * <p>Run a Jet analysis job to examine the journal of updates to
     * HTTP sessions. As we don't want it to run for ever, give it a
     * second or two to do some work then end it.
     * </p>
     */
    @ShellMethod(key = "ANALYSIS", value = "Analyse the orders")
    public void analyseSessions() throws Exception {
        IMap<String, Integer> sequenceMap = this.hazelcastInstance.getMap(Constants.IMAP_NAME_SEQUENCE);

        sequenceMap.clear();

        Job analysisJob = this.jetInstance.newJob(SequenceAnalysis.build());

        analysisJob.getFuture();

        // Give the job time to do something
        int wait = 2;
        log.info("Sleep {} seconds", wait);
        TimeUnit.SECONDS.sleep(wait);

        if (analysisJob.getStatus() == JobStatus.RUNNING) {
            analysisJob.cancel();
        } else {
            log.error("Job status {}", analysisJob.getStatus());
        }

        System.out.printf("%d sequence%s found%n", sequenceMap.size(), (sequenceMap.size() == 1 ? "" : "s"));
    }

    /**
     * <p>List IMaps in name order, and the data contained in each.
     * </p>
     * <p>Note these are {@link com.hazelcast.core.IMap#get IMap#get} or
     * <b><i>read</i></b> operations. They won't interfere with session expiry as this
     * is set on <b><i>write</i></b>.
     * </p>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ShellMethod(key = "LIST", value = "List map keys")
    public void listIMaps() {
        Set<String> iMapNames = this.hazelcastInstance.getDistributedObjects().stream()
                .filter(distributedObject -> distributedObject instanceof IMap)
                .filter(distributedObject -> !distributedObject.getName().startsWith(Jet.INTERNAL_JET_OBJECTS_PREFIX))
                .map(distributedObject -> distributedObject.getName()).collect(Collectors.toCollection(TreeSet::new));

        iMapNames.stream().forEach(name -> {
            IMap<?, ?> iMap = this.hazelcastInstance.getMap(name);

            System.out.println("");
            System.out.printf("IMap: '%s'%n", name);

            // Sort if possible
            Set<?> keys = iMap.keySet();
            if (!keys.isEmpty() && keys.iterator().next() instanceof Comparable) {
                keys = new TreeSet(keys);
            }

            keys.stream().forEach(key -> {
                System.out.printf("    -> '%s' -> %s%n", key, iMap.get(key));
            });

            System.out.printf("[%d entr%s]%n", iMap.size(), (iMap.size() == 1 ? "y" : "ies"));
        });

        System.out.println("");
    }

}
