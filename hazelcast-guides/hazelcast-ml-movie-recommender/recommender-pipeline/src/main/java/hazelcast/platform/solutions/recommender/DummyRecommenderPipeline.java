package hazelcast.platform.solutions.recommender;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
import com.hazelcast.nio.serialization.genericrecord.GenericRecordBuilder;

import java.util.ArrayList;
import java.util.Map;

public class DummyRecommenderPipeline {
    public static void main(String []args){
        Pipeline pipeline = DummyRecommenderPipeline.createPipeline();
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("dummy recommender");
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        hz.getJet().newJob(pipeline, jobConfig);
    }

    private static final String REQUEST_MAP_NAME = "recommendation_request";
    private static final String RESPONSE_MAP_NAME = "recommendation_response";

    private static Pipeline createPipeline(){
        Pipeline pipeline = Pipeline.create();

        StreamStage<Map.Entry<String, String>> requests =
                pipeline.readFrom(Sources.<String, String>mapJournal(REQUEST_MAP_NAME, JournalInitialPosition.START_FROM_CURRENT))
                        .withIngestionTimestamps()
                        .setName("requests");


        StreamStage<Tuple2<String, ArrayList<GenericRecord>>> recommendations =
                requests.map(req -> Tuple2.tuple2(req.getKey(), dummyRecommendations()))
                        .setName("recommendations");

        recommendations.writeTo(Sinks.map(RESPONSE_MAP_NAME));

        return pipeline;
    }

    private static ArrayList<GenericRecord> dummyRecommendations(){
        ArrayList<GenericRecord> result = new ArrayList<>();

        GenericRecord recommendation1 = GenericRecordBuilder.compact("recommendation")
                .setString("title", "GoldenEye")
                .setString("image_url","https://images-na.ssl-images-amazon.com/images/M/MV5BMzk2OTg4MTk1NF5BMl5BanBnXkFtZTcwNjExNTgzNA@@..jpg")
                .build();
        result.add(recommendation1);

        GenericRecord recommendation2 = GenericRecordBuilder.compact("recommendation")
                .setString("title", "Desperado")
                .setString("image_url","Desperado\",\"https://images-na.ssl-images-amazon.com/images/M/MV5BYjA0NDMyYTgtMDgxOC00NGE0LWJkOTQtNDRjMjEzZmU0ZTQ3XkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg")
                .build();
        result.add(recommendation2);

        GenericRecord recommendation3 = GenericRecordBuilder.compact("recommendation")
                .setString("title", "Four Rooms")
                .setString("image_url","https://images-na.ssl-images-amazon.com/images/M/MV5BNDc3Y2YwMjUtYzlkMi00MTljLTg1ZGMtYzUwODljZTI1OTZjXkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg")
                .build();
        result.add(recommendation3);

        GenericRecord recommendation4 = GenericRecordBuilder.compact("recommendation")
                .setString("title", "Mad Love")
                .setString("image_url","https://images-na.ssl-images-amazon.com/images/M/MV5BNDE0NTQ1NjQzM15BMl5BanBnXkFtZTYwNDI4MDU5..jpg")
                .build();
        result.add(recommendation4);

        GenericRecord recommendation5 = GenericRecordBuilder.compact("recommendation")
                .setString("title", "The Aristocats")
                .setString("image_url","https://images-na.ssl-images-amazon.com/images/M/MV5BMTU1MzM0MjcxMF5BMl5BanBnXkFtZTgwODQ0MzcxMTE@..jpg")
                .build();
        result.add(recommendation5);

        GenericRecord recommendation6 = GenericRecordBuilder.compact("recommendation")
                .setString("title", "Life of Brian")
                .setString("image_url","https://images-na.ssl-images-amazon.com/images/M/MV5BMzAwNjU1OTktYjY3Mi00NDY5LWFlZWUtZjhjNGE0OTkwZDkwXkEyXkFqcGdeQXVyMTQxNzMzNDI@..jpg")
                .build();
        result.add(recommendation6);

        return result;
    }
}
