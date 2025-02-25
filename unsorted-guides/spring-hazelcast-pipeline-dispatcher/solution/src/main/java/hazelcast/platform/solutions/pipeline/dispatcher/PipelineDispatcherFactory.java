package hazelcast.platform.solutions.pipeline.dispatcher;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import hazelcast.platform.solutions.pipeline.dispatcher.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PipelineDispatcherFactory implements
        EntryAddedListener<String, String>,
        EntryRemovedListener<String,String>,
        EntryUpdatedListener<String,String> {

    private static final Logger log = LoggerFactory.getLogger(PipelineDispatcherFactory.class);

    public static final String ROUTER_CONFIG_MAP = "router_config";

    @Value("${hazelcast.pipeline.dispatcher.embed_hazelcast:false}")
    private boolean embedHazelcast;

    // the maximum amount of time, in milliseconds to wait before returning a timeout error
    @Value("${hazelcast.pipeline.dispatcher.request_timeout_ms:3000}")
    private long requestTimeoutMs;

    public <R,P> PipelineDispatcher<R,P> dispatcherFor(String name){
        PipelineDispatcher<R,P> result = dispatcherMap.computeIfAbsent(name, k -> {
            String routerConfig = getRouterConfigFor(k);
            RequestRouter rr;
            if (routerConfig != null){
                // currently, the only type of router supported is the MultiVersionRequestRouter, so we assume here
                // that any entry in the ROUTER_CONFIG_MAP map is a MultiVersionRequestRouterConfig instance
                rr = new WeightedRouter(k, routerConfig);
            } else {
                rr = new DefaultRequestRouter(k);
            }
            return new PipelineDispatcher<R,P>(
                    this.requestKeyFactory,
                    hazelcastInstance,
                    name,
                    rr,
                    3000);
        });
        //TODO it seems like requestTimeoutMs is not getting populated so for now it is hard-coded


        return result;
    }

    private ConcurrentHashMap<String, PipelineDispatcher> dispatcherMap;

    private RequestKeyFactory requestKeyFactory;

    private HazelcastInstance hazelcastInstance;

    @PostConstruct
    public void initialize() {
        this.dispatcherMap = new ConcurrentHashMap<>();

        this.requestKeyFactory = new RequestKeyFactory();

        // create the hazelcast instance
        this.hazelcastInstance = HazelcastUtil.buildHazelcastInstance(embedHazelcast);

        hazelcastInstance.getMap(ROUTER_CONFIG_MAP).addEntryListener(this, true);
    }

    /**
     * Retrieves the router configuration.  May return null.
     */
    private String getRouterConfigFor(String name){
        return hazelcastInstance.<String,String>getMap(ROUTER_CONFIG_MAP).get(name);
    }

    public HazelcastInstance getEmbeddedHazelcastInstance(){
        if (embedHazelcast)
            return hazelcastInstance;
        else
            return null;
    }

    @PreDestroy
    public void close(){
        hazelcastInstance.shutdown();
    }

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        String name = event.getKey();
        handleAddUpdate(name, event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        String name = event.getKey();
        handleAddUpdate(name,  event.getValue());
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        String name = event.getKey();
        handleRemove(name);
    }

    private <R,P> void handleAddUpdate(String name, String config){
        // currently, only the WeightedRouter is supported
        RequestRouter rr =  new WeightedRouter(name, config);
        dispatcherMap.put(name,
                new PipelineDispatcher<R,P>(this.requestKeyFactory, hazelcastInstance, name, rr, requestTimeoutMs));

        log.info("Received routing update for \"" + name + "\" : " + config);
    }

    private <R,P> void handleRemove(String name){
        RequestRouter rr = new DefaultRequestRouter(name);
        dispatcherMap.put(name, new PipelineDispatcher<R,P>(this.requestKeyFactory, hazelcastInstance, name, rr, requestTimeoutMs));
        log.info("Set routing policy for \"" + name + "\" to default.");
    }
}
