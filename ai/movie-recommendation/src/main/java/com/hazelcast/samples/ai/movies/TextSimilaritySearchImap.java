/*
 * Copyright (c) 2008-2024, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.samples.ai.movies;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.util.Timer;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This demo generates embeddings of movie plot summaries from the
 * <a href="https://www.cs.cmu.edu/~ark/personas/">"CMU Movies Summary Corpus"</a>. The embeddings
 * are stored in an IMap, along with movie metadata (title, release date).
 * We then perform similarity search on user's input strings from console and list top 10 movies
 * with most similar plot summaries.
 *
 * <p>Dependencies:</p>
 * <ul>
 *     <li>langchain4j for the <a href="https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2">all-miniLM-L6-v2</a>
 *     model that transforms text to 384 dimensional dense vector</li>
 * </ul>
 */
public class TextSimilaritySearchImap {

    static Map<String, String> movieIdToPlotSummary = new ConcurrentHashMap<>();
    static Map<String, MovieMetadata> movieIdToMeta = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        // instantiate embedding model
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // start hazelcast
        IMap<String, MovieMetadata> movies = startHzAndGetMovieImap();
        // uncomment following line if you have a running cluster and want to execute this demo with a Hazelcast client
//         IMap<String, MovieMetadata> movies = startHzClientAndGetMovieImap();
        // index not necessary for a small IMap
//        movies.addIndex(IndexType.SORTED, "releaseDate");

        // ingest
        loadSummaries(movies);
        generateEmbeddings(movies, embeddingModel);

        // read user input from console and search matches
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                // read user input
                System.out.println("Enter release date pattern followed by search string:");
                String line = consoleReader.readLine();
                var parts = line.split(" ", 2);
                if (parts.length != 2) {
                    System.out.println("Invalid search string");
                    continue;
                }

                String yearPattern = parts[0];
                String userInput = parts[1];

                // transform user input to vector
                float[] query = embeddingModel.embed(userInput).content().vector();

                // find & output top 10 similar matches of plot summary to given text
                // this invocation uses the prototype nearestNeighbours predicate.
//                Collection<MovieMetadata> results = movies.values(VectorPredicates.<String, MovieMetadata>nearestNeighbours(10)
//                        .to(query)
//                        .withMetric(Metric.COSINE)
//                        // some movies lack plot summary
//                        .withOptionalEmbedding("vector")
//                        // release date sometimes is only a year, sometimes full yyyy-mm-dd
//                        .matching(Predicates.like("releaseDate", yearPattern))
//                        .build());

                // find & output top 10 similar matches of plot summary to given text
                // this invocation uses already available Predicate API predicates.
                Collection<MovieMetadata> results = movies.values(Predicates.pagingPredicate(
                        Predicates.and(
                                // some movies lack plot summary
                                (Predicate<String, MovieMetadata>) mapEntry -> mapEntry.getValue().vector != null,
                                // release date sometimes is only a year, sometimes full yyyy-mm-dd
                                Predicates.like("releaseDate", yearPattern)),
                        // find 10 closest neighbours
                        new MovieDistanceComparator<>(query), 10));

                System.out.println("Found those results for you:");
                AtomicInteger index = new AtomicInteger(1);
                results.forEach(result -> {
                    String plotSummarySubstring = result.plotSummary;
                    plotSummarySubstring = plotSummarySubstring.substring(0, Math.min(plotSummarySubstring.length(), 130));
                    System.out.printf("%d) title: \"%s\"\trelease date: \"%s\"\tplot: \"%s..\"%n\n", index.getAndIncrement(),
                            result.name, result.releaseDate, plotSummarySubstring);
                });
            }
        }
    }

    /**
     * Starts an embedded Hazelcast member and returns the movies {@code IMap}.
     */
    private static IMap<String, MovieMetadata> startHzAndGetMovieImap() {
        Config config = new Config();
        HazelcastInstance member = Hazelcast.newHazelcastInstance(config);
        member.getConfig().addMapConfig(new MapConfig().setName("movies").setInMemoryFormat(InMemoryFormat.OBJECT));
        return member.getMap("movies");
    }

    /**
     * Starts a Hazelcast client to connect to an existing cluster and returns the movies {@code IMap}.
     */
    private static IMap<String, MovieMetadata> startHzClientAndGetMovieImap() {
        // client side
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        return client.getMap("movies");
    }

    //region data loading
    private static void loadSummaries(IMap<String, MovieMetadata> movies) {
        // read files
        preparePlotSummaries();

        long start = Timer.nanos();
        System.out.println("Loading " + movieIdToMeta.size() + " plot summaries");
        movies.putAll(movieIdToMeta);
        System.out.println("Loading " + movieIdToMeta.size() + " plot summaries took " + Timer.millisElapsed(start) + " milliseconds");
    }

    /**
     * Reads the movie & plot summary data
     */
    public static void preparePlotSummaries() {
        Scanner scanner = new Scanner(TextSimilaritySearchImap.class.getResourceAsStream("plot_summaries.txt"));
        Pattern pattern = Pattern.compile("(\\d+)\t(.*)\n");
        scanner.findAll(pattern)
                .forEach(matchResult -> {
                    String id = matchResult.group(1);
                    String plot = matchResult.group(2);
                    movieIdToPlotSummary.put(id, plot);
                });
        scanner = new Scanner(TextSimilaritySearchImap.class.getResourceAsStream("movie.metadata.tsv"));
        pattern = Pattern.compile("(\\d+)\t[^\t]*\t([^\t]*)\t([^\t]*)\t.*\n");
        scanner.findAll(pattern)
                .forEach(matchResult -> {
                    String id = matchResult.group(1);
                    String name = matchResult.group(2);
                    String releaseDate = matchResult.group(3);
                    movieIdToMeta.put(id, new MovieMetadata(name, releaseDate, movieIdToPlotSummary.get(id)));
                });
    }
    //endregion

    public static void generateEmbeddings(IMap<String, MovieMetadata> movies, EmbeddingModel embeddingModel) {
        long start = Timer.nanos();
        System.out.println("Generating embeddings for " + movies.size() + " plot summaries");

        int embeddings = movies.executeOnEntries(entry -> {
            MovieMetadata value = entry.getValue();
            if (value.plotSummary != null) {
                value.vector = embeddingModel.embed(value.plotSummary).content().vector();
                entry.setValue(value);
                return 1;
            }
            return 0;
        }).values().stream().mapToInt(i -> i).sum();
        System.out.println("Generating embeddings for " + embeddings + " plot summaries took " + Timer.secondsElapsed(start) + " seconds");
    }

    public static class MovieMetadata implements Serializable {
        private final String name;
        private final String releaseDate;
        private final String plotSummary;
        /**
         * Plot summary vector embedding
         */
        private float[] vector;

        public MovieMetadata(String name, String releaseDate, String plotSummary) {
            this.name = name;
            this.releaseDate = releaseDate;
            this.plotSummary = plotSummary;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public float[] getVector() {
            return vector;
        }
    }

    public static class MovieDistanceComparator<K> implements Comparator<Map.Entry<K, MovieMetadata>>, Serializable {

        // query vector
        private final float[] query;
        // cache distances to avoid repeated evaluation during sorting
        private final transient Map<K, Float> distanceCache;

        public MovieDistanceComparator(float[] query) {
            this.query = query;
            this.distanceCache = new ConcurrentHashMap<>(1024);
        }

        /**
         * @return result of comparison which entry is closer to query vector
         */
        @Override
        public int compare(Map.Entry<K, MovieMetadata> e1, Map.Entry<K, MovieMetadata> e2) {
            var d1 = getDistanceTo(e1);
            var d2 = getDistanceTo(e2);
            return -Float.compare(d1, d2);
        }

        private float getDistanceTo(Map.Entry<K, MovieMetadata> entry) {
            var key = entry.getKey();

            var maybeDistance = distanceCache.get(key);
            if (maybeDistance != null) {
                return maybeDistance;
            }

            var distance = cosineScore(query, entry.getValue().getVector());
            distanceCache.put(key, distance);
            return distance;
        }

        /**
         * Simple but not the most efficient computation of cosine distance
         * @return distance in -1..1 range
         */
        private static float cosineDistance(float[] a, float[] b) {
            float sum = 0.0f;
            float norm1 = 0.0f;
            float norm2 = 0.0f;
            for (int i = 0; i < a.length; i++) {
                sum += a[i] * b[i];
                norm1 += a[i] * a[i];
                norm2 += b[i] * b[i];
            }
            return (float) (sum / Math.sqrt(norm1 * norm2));
        }

        /**
         * @return score in 0..1 range
         */
        private static float cosineScore(float[] a, float[] b) {
            return (1 + cosineDistance(a, b)) / 2;
        }
    }
}
