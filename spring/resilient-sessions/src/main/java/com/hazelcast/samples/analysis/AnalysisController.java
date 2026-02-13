package com.hazelcast.samples.analysis;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.Observable;
import com.hazelcast.jet.core.ProcessorMetaSupplier;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.IMap;
import com.hazelcast.samples.model.Basket;
import com.hazelcast.spring.session.BackingMapSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.hazelcast.jet.pipeline.ServiceFactories.nonSharedService;
import static com.hazelcast.samples.ShopConfiguration.SESSION_MAP_NAME;

@RestController
public class AnalysisController {

    /**
     * Example value of minimal price to send the notification.
     * Typically in production use it would come from some environment configuration.
     */
    private static final BigDecimal MINIMAL_PRICE_FOR_NOTIF = new BigDecimal(100);

    /**
     * Duration after which session is marked as "Stale".
     * Intentionally very low, to make testing easier.
     */
    private static final Duration MAX_INACTIVE_DURATION = Duration.ofSeconds(10);

    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public AnalysisController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @RequestMapping("/basket/analyze")
    public String analyze()
            throws ExecutionException, InterruptedException {
        JetService jet = hazelcastInstance.getJet();
        Observable<SessionReminderDto> analyzeResults = jet.newObservable();
        IMap<Long, BackingMapSession> sessions = hazelcastInstance.getMap(SESSION_MAP_NAME);
        var pipeline = Pipeline.create();
        ServiceFactory<?, HazelcastInstance> serviceFactory = nonSharedService(ProcessorMetaSupplier.Context::hazelcastInstance);
        pipeline.readFrom(Sources.map(sessions))
                .map(Map.Entry::getValue)
                .mapUsingService(serviceFactory, (hz, s) -> {
                    //noinspection DataFlowIssue
                    var basket = s.getAttribute("basket").deserialize(hz, Basket.class);

                    Instant lastAccessedTime = s.getLastAccessedTime();
                    Duration howLongInactive = Duration.between(lastAccessedTime, Instant.now());
                    String timeStatus = howLongInactive.compareTo(MAX_INACTIVE_DURATION) > 0
                            ? "STALE"
                            : "OK";

                    BigDecimal totalValue = basket.items().stream()
                                                  .map(item -> item.orderPrice().multiply(new BigDecimal(item.quantity())))
                                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new SessionReminderDto(s.getId(), s.getPrincipalName(), timeStatus, basket.items(), totalValue);
                })
                .filter(dto -> dto.totalPrice().compareTo(MINIMAL_PRICE_FOR_NOTIF) > 0 && dto.timeStatus().equals("STALE"))
                .peek()
                .writeTo(Sinks.observable(analyzeResults));

        Job job = jet.newJob(pipeline);
        job.join();

        CompletableFuture<List<SessionReminderDto>> future = analyzeResults.toFuture(Stream::toList);
        List<SessionReminderDto> stats = future.get();

        analyzeResults.destroy();
        return stats.toString();
    }
}
