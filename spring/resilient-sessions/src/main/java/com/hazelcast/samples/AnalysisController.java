package com.hazelcast.samples;

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
import com.hazelcast.samples.model.BasketItem;
import com.hazelcast.spring.session.BackingMapSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.hazelcast.jet.pipeline.ServiceFactories.nonSharedService;
import static com.hazelcast.samples.ShopConfiguration.SESSION_MAP_NAME;

@RestController
public class AnalysisController {

    private static final BigDecimal MINIMAL_PRICE_FOR_NOTIF = new BigDecimal(100);

    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public AnalysisController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    record SessionReminderDto(String sessionId, String userId, String timeStatus, List<BasketItem> items, BigDecimal totalPrice) {
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
                    String timeStatus = "OK";
                    if (Instant.now().isAfter(lastAccessedTime.plusSeconds(30))) {
                        timeStatus = "STALE";
                    }

                    BigDecimal totalValue = basket.items().stream()
                                                  .map(item -> item.orderPrice().multiply(new BigDecimal(item.quantity())))
                                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new SessionReminderDto(s.getId(), s.getPrincipalName(), timeStatus, basket.items(), totalValue);
                })
                .filter(dto -> dto.totalPrice.compareTo(MINIMAL_PRICE_FOR_NOTIF) > 0 && dto.timeStatus().equals("STALE"))
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
