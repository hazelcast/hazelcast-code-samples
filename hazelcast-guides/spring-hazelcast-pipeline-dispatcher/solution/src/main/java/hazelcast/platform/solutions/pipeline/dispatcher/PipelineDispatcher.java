package hazelcast.platform.solutions.pipeline.dispatcher;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import hazelcast.platform.solutions.pipeline.dispatcher.internal.RequestKeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ConcurrentHashMap;

/**
 * PipelineDispatcher is designed to be a singleton scoped bean
 */

public class PipelineDispatcher<R,P> implements EntryAddedListener<String,P> {
    private static final Logger log = LoggerFactory.getLogger(PipelineDispatcher.class);
    RequestKeyFactory requestKeyFactory;

    private final String clientId;

    private final RequestRouter requestRouter;

    private final ConcurrentHashMap<String, DeferredResult<P>> pendingRequestMap;

    private final HazelcastInstance hz;

    private final long requestTimeoutMs;

    public PipelineDispatcher(
            RequestKeyFactory requestKeyFactory,
            HazelcastInstance hz,
            String name,
            RequestRouter requestRouter,
            long requestTimeoutMs){
        this.requestTimeoutMs = requestTimeoutMs;
        this.requestKeyFactory = requestKeyFactory;
        this.pendingRequestMap = new ConcurrentHashMap<>();

        this.clientId = requestKeyFactory.newRandomClientId();
        this.requestRouter = requestRouter;
        this.hz = hz;

        // add the response listener to the response map
        IMap<String, P> responseMap = hz.getMap(name + "_response");
        Predicate<String, P> myRequests = Predicates.like("__key", clientId + "%");
        responseMap.addEntryListener(this, myRequests, true);
    }

    @Override
    public void entryAdded(EntryEvent<String, P> entryEvent) {
        log.trace("Received response for {}", entryEvent.getKey());
        DeferredResult<P> result = pendingRequestMap.get(entryEvent.getKey());
        if (result != null){
            log.info("RECEIVED " + entryEvent.getKey() + " : (" + entryEvent.getValue().getClass().getName() + ")");
            result.setResult(entryEvent.getValue());
        } else {
            log.warn("Could not find a pending request for {}", entryEvent.getKey());
        }
    }

    public DeferredResult<P> send(R  request){
        String key = requestKeyFactory.newRequestKey(this.clientId);
        DeferredResult<P> result = new DeferredResult<>(requestTimeoutMs);
        result.onTimeout(() -> result.setErrorResult(
                ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timeout occurred.")));
        IMap<String,R> requestMap = hz.getMap(requestRouter.getRequestMapName());
        pendingRequestMap.put(key, result);
        requestMap.putAsync(key, request);
        log.trace("Sent request {}", key);
        return result;
    }
}
