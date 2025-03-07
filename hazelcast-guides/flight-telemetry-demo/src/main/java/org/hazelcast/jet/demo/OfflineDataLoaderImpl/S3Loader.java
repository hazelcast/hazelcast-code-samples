package org.hazelcast.jet.demo.OfflineDataLoaderImpl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.core.JobStatus;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.s3.S3Sources;
import com.hazelcast.map.IMap;
import org.hazelcast.jet.demo.FlightDataSourceImpl.OfflineDataSource;
import org.hazelcast.jet.demo.IOfflineDataLoader;
import org.hazelcast.jet.demo.util.Util;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static com.hazelcast.jet.Util.entry;
import static java.util.Collections.singletonList;

public class S3Loader implements IOfflineDataLoader {

    public long loadData(String regionName, HazelcastInstance hzInstance, String path) {
        AtomicLong minEpoch = new AtomicLong();
        long dataLoadStartEpoch = System.currentTimeMillis();

        IMap<String, HazelcastJsonValue> offlineDataMap = hzInstance.getMap(regionName + OfflineDataSource.OFFLINE_DATA_MAP_SUFFIX);

        offlineDataMap.clear();

        Pipeline p = Pipeline.create();
        p.readFrom(S3Sources.s3(singletonList(path), Util.getFlightDataFilePrefix(regionName),
                        () -> S3Client.builder().credentialsProvider(AnonymousCredentialsProvider.create()).region(Region.US_EAST_2).build()))
                .map(line -> {
                            return entry(UUID.randomUUID().toString(), new HazelcastJsonValue(line));
                        }
                )
                .writeTo(Sinks.map(regionName + OfflineDataSource.OFFLINE_DATA_MAP_SUFFIX));

        JetService jetService = hzInstance.getJet();
        Job job = jetService.newJob(p, new JobConfig().setName("FlightTelemetryOfflineDataLoad-" + regionName)
                .setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE));

        job.join();

        offlineDataMap.values().stream().distinct().forEach(val -> {
            JsonObject object = Json.parse(val.toString()).asObject();
            long updateEpoch = object.getLong("now", 0L);

            if (updateEpoch > 0L && (updateEpoch < minEpoch.get() || minEpoch.get() == 0)) {
                minEpoch.set(updateEpoch);
            }
        });
        System.out.println("Successfully loaded offline data. A total of [" + offlineDataMap.entrySet().size() + "] flight positions were loaded for [" + regionName + "] in " + (System.currentTimeMillis() - dataLoadStartEpoch) + "ms");

        return minEpoch.get();
    }
}
