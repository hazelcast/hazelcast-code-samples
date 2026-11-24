package com.hazelcast.samples.sql;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.core.ProcessorMetaSupplier;
import com.hazelcast.jet.pipeline.JournalInitialPosition;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.session.BackingMapSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import static com.hazelcast.jet.pipeline.ServiceFactories.nonSharedService;
import static com.hazelcast.samples.ShopConfiguration.SESSION_MAP_NAME;

/**
 * Example works only if all classes are present on the classpath - therefore only when using no WAN replication
 * (that requires separate docker instances; adding classes to their classpath would be an unnecessary complication).
 */
@Component
@ConditionalOnExpression("${use.wan.replication:false} == false")
public class SqlAnalysisETL {

    private final HazelcastInstance hazelcastInstance;
    private Job etlJob;

    @Autowired
    public SqlAnalysisETL(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @PostConstruct
    public void startEtl() {
        JetService jet = hazelcastInstance.getJet();
        IMap<Long, BackingMapSession> sessions = hazelcastInstance.getMap(SESSION_MAP_NAME);
        var pipeline = Pipeline.create();
        ServiceFactory<?, HazelcastInstance> serviceFactory = nonSharedService(ProcessorMetaSupplier.Context::hazelcastInstance);
        pipeline.readFrom(Sources.mapJournal(sessions, JournalInitialPosition.START_FROM_OLDEST))
                .withIngestionTimestamps()
                .map(Utils::getEntryValue)
                .flatMapUsingService(serviceFactory, Utils::expandSessionObject)
                .writeTo(Sinks.map("sessionsExpanded"));

        etlJob = jet.newJob(pipeline, new JobConfig().addClass(Utils.class)
                                                     .addClass(SqlAnalysisETL.class)
                                                     .addClass(BasketItemDto.class)
                                                     .addClass(BackingMapSession.class)
        );

        hazelcastInstance.getSql().executeUpdate("""
                CREATE OR REPLACE MAPPING sessionsExpanded (
                    __key varchar,
                    principalName varchar,
                    lastAccessedTime TIMESTAMP,
                    productId bigint,
                    productName varchar,
                    listPrice decimal,
                    orderPrice decimal,
                    quantity int
                ) TYPE IMap
                OPTIONS (
                    'keyFormat' = 'varchar',
                    'valueFormat' = 'java',
                    'valueJavaClass' = 'com.hazelcast.samples.sql.BasketItemDto'
                )
                """);
        System.out.println("Init done");
    }

    @PreDestroy
    public void destroy() {
        etlJob.cancel();
    }

}
