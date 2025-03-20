package com.hazelcast.samples.ai.movies;

import com.hazelcast.map.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieEmbeddingEntryProcessorTest extends HazelcastTestSupport {
    @Test
    public void entryProcessorUpdatesEmbeddingOnBackup() {
        var instances = createHazelcastInstances(2);
        assertClusterSizeEventually(2, instances);

        // given
        var key = generateKeyOwnedBy(instances[0]);
        IMap<String, TextSimilaritySearchImap.MovieMetadata> map0 = instances[0].getMap("map");
        map0.put(key, new TextSimilaritySearchImap.MovieMetadata("title", "2000", "dummy summary"));

        // when
        assertThat(map0.executeOnKey(key, new TextSimilaritySearchImap.MovieEmbeddingEntryProcessor())).isEqualTo(1);
        instances[0].getLifecycleService().terminate();

        // then
        IMap<String, TextSimilaritySearchImap.MovieMetadata> map1 = instances[1].getMap("map");
        assertThat(map1.get(key).getVector()).as("Embedding should be replicated to backup").hasSize(384);
    }
}
