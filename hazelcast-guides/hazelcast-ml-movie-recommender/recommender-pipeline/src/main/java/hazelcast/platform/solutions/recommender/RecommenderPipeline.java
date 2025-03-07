package hazelcast.platform.solutions.recommender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.jet.python.PythonServiceConfig;
import com.hazelcast.jet.python.PythonTransforms;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
import com.hazelcast.nio.serialization.genericrecord.GenericRecordBuilder;
import com.hazelcast.replicatedmap.ReplicatedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RecommenderPipeline {
    private static final Logger log = Logger.getLogger(RecommenderPipeline.class.getName());

    // takes one argument: the python base dir, which will be interpreted on the hazelcast node
    public static void main(String[] args) {
        if (args.length != 1){
            System.err.println("Required argument missing.  Please pass the python base directory as the first and only program argument");
            System.exit(1);
        }

        Pipeline pipeline = RecommenderPipeline.createPipeline(args[0]);
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("Recommender");
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        hz.getJet().newJob(pipeline, jobConfig);
    }

    private static final String REQUEST_MAP_NAME = "recommendation_request";
    private static final String RESPONSE_MAP_NAME = "recommendation_response";

    private static Pipeline createPipeline(String pythonBaseDir) {
        Pipeline pipeline = Pipeline.create();

        // requests come in as simple strings containing a title
        StreamStage<Map.Entry<String, String>> requests =
                pipeline.readFrom(Sources.<String, String>mapJournal(REQUEST_MAP_NAME, JournalInitialPosition.START_FROM_CURRENT))
                        .withoutTimestamps()
                        .setName("requests");

        // recommendations look like: requestKey|{JSON ENCODED RECOMMENDATION LIST}
        StreamStage<String> recommendations = requests.map(entry -> entry.getKey() + "," + entry.getValue()).apply(PythonTransforms.mapUsingPython(new PythonServiceConfig()
                .setBaseDir(pythonBaseDir)
                .setHandlerModule("manyFromOneRec")
                .setHandlerFunction("do_recommender"))).setName("Call Python Recommender");

        ServiceFactory<?, ObjectMapper> objectMapperServiceFactory =
                ServiceFactories.nonSharedService(ctx -> new ObjectMapper());

        // the first item in the tuple is the requestId, the next is a list of movie title, ml_id tuples
        StreamStage<Tuple2<String, List<Tuple2<String, Integer>>>> parsedRecommendations =
                recommendations.mapUsingService(objectMapperServiceFactory, RecommenderPipeline::parseRecommenderOutput)
                        .setName("Parse Recommender Results");

        StreamStage<Tuple2<String, ArrayList<GenericRecord>>> recommendationsWithPosters =
                parsedRecommendations.mapUsingService(
                        ServiceFactories.replicatedMapService("movie_posters"),
                        RecommenderPipeline::lookupMoviePosters)
                        .setName("recommendations with posters");

        recommendationsWithPosters.writeTo(Sinks.map(RESPONSE_MAP_NAME)).setName("write to map");

        return pipeline;
    }

    private static Tuple2<String, ArrayList<GenericRecord>> lookupMoviePosters(ReplicatedMap<Integer, String> posterMap, Tuple2<String, List<Tuple2<String, Integer>>> recommendations) {
        ArrayList<GenericRecord> result = new ArrayList<>();
        for(Tuple2<String,Integer> recommendation: recommendations.f1()){
            String imageURL = posterMap.getOrDefault(recommendation.f1(), "");
            GenericRecord r = GenericRecordBuilder.compact("recommendation")
                    .setString("title", recommendation.f0())
                    .setString("image_url", imageURL)
                    .build();
            result.add(r);
        }
        return Tuple2.tuple2(recommendations.f0(), result);
    }

    // returns (requestid, [ (title_1, ml_movie_id_1), (title_2, ml_movie_id_2)])
    private static Tuple2<String, List<Tuple2<String, Integer>>> parseRecommenderOutput(ObjectMapper mapper, String recommendation) {
        int i = recommendation.indexOf(',');
        if (i == -1) {
            log.severe("Could not parse recommender response: " + recommendation);
            return Tuple2.tuple2("error", new ArrayList<>());
        }

        String key = recommendation.substring(0, i);
        String recommendationAsJSON = recommendation.substring(i + 1);
        ArrayList<Tuple2<String, Integer>> result = new ArrayList<>();
        try {
            JsonNode rootNode = mapper.readTree(recommendationAsJSON);
            JsonNode data = rootNode.get("data");
            if (data == null || !data.isArray()) {
                log.severe("Could not parse recommender response. No \"data\" node found in recommendation or data is not an array: " + recommendationAsJSON);
                return Tuple2.tuple2(key, new ArrayList<>());
            }

            for (JsonNode node : data) {
                result.add(Tuple2.tuple2(node.get("title").asText(), node.get("movie_id_ml").asInt()));
            }
        }catch(JsonProcessingException jpx){
            log.severe("Could not parse recommender response. No \"data\" node found in recommendation or data is not an array: " + recommendationAsJSON);
            return Tuple2.tuple2(key, new ArrayList<>());
        }
        return Tuple2.tuple2(key, result);
    }
}