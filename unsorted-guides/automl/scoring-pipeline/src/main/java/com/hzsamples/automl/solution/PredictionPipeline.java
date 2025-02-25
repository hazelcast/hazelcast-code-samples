package com.hzsamples.automl.solution;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import com.hzsamples.automl.AuthRequestv01;
import com.hzsamples.automl.AutoMLTabularPredictionClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.hazelcast.jet.datamodel.Tuple2.tuple2;

public class PredictionPipeline {
    public static Pipeline buildPipeline(String modelProject, String modelLocation, String modelEndpointId, float fraudConfidenceThreshold, GoogleCredentials credentials) {
        Pipeline result = Pipeline.create();

        // read input authorization requests serialized as byte []
        StreamStage<Map.Entry<String, byte[]>> serializedAuthRequests = result.readFrom(
                        Sources.<String, byte[]>mapJournal("auth_requests", JournalInitialPosition.START_FROM_OLDEST))
                .withIngestionTimestamps().setName("Input");

        // deserialize the input into a java POJO
        StreamStage<AuthRequestv01> authRequests =
                serializedAuthRequests.map(entry -> AuthRequestv01.parseFrom(entry.getValue()))
                        .setName("deserialize Proto");

        // extract the fields of interest from the POJO and format as a protobuf Struct as required for the Vertex AI endpoint
        StreamStage<Tuple2<AuthRequestv01, Struct>> authReqProtos =
                authRequests.map(authReq -> tuple2(authReq, authRequestToFeature(authReq)))
                        .setName("map to predict api features");

        // create a custom prediction service to call a Vertex AI endpoint in Google Cloud
        // this is a thin wrapper around the Vertex AI java API
        ServiceFactory<?, AutoMLTabularPredictionClient> predictionService =
                ServiceFactories.nonSharedService(c -> new AutoMLTabularPredictionClient(
                        modelProject,
                        modelLocation,
                        modelEndpointId,
                        credentials)).toNonCooperative();

        // use the custom prediction service to obtain a PredictionResponse, wrap the response in a helper class for ease of use
        StreamStage<Tuple2<AuthRequestv01, AutoMLTabularPredictionClient.PredictResponseExtractor>> predictions
                = authReqProtos.mapUsingService(predictionService, (ps, tuple) -> tuple2(tuple.f0(), ps.predict(tuple.f1())))
                .setName("call predict api");

        // filter out predictions that yielded an exception for any reason
        StreamStage<Tuple2<AuthRequestv01, AutoMLTabularPredictionClient.PredictResponseExtractor>> goodPredictions
                = predictions.filter(t2 -> t2.f1().isSucceeded()).setName("filter out exceptions");

        // based on the confidence scores in the prediction response, make a fraud prediction
        StreamStage<Tuple2<AuthRequestv01, Boolean>> decisions =
                goodPredictions.map((tuple) -> tuple2(tuple.f0(), tuple.f1().getPrediction(0, "1") > fraudConfidenceThreshold))
                        .setName("classify");

        // output the decision
        decisions.writeTo(Sinks.logger((t) -> (t.f1() ? "DECLINED " : "APPROVED") + t.f0().getAmt() + " on " + t.f0().getCategory() + " in " + t.f0().getCity() + ", " + t.f0().getState()));
        return result;
    }

    public  static Value stringValue(String v){
        return Value.newBuilder().setStringValue(v).build();
    }

    public static Struct authRequestToFeature(AuthRequestv01 authReq){
        return Struct.newBuilder()
                .putFields("gender", stringValue(authReq.getGender()))
                .putFields("city", stringValue(authReq.getCity()))
                .putFields("state", stringValue(authReq.getState()))
                .putFields("lat", stringValue(authReq.getLat()))
                .putFields("long", stringValue(authReq.getLong()))
                .putFields("city_pop", stringValue(authReq.getCityPop()))
                .putFields("job", stringValue(authReq.getJob()))
                .putFields("dob", stringValue(authReq.getDob()))
                .putFields("category", stringValue(authReq.getCategory()))
                .putFields("amt", stringValue(authReq.getAmt()))
                .putFields("merchant", stringValue(authReq.getMerchant()))
                .putFields("merch_lat", stringValue(authReq.getMerchLat()))
                .putFields("merch_long", stringValue(authReq.getMerchLong())).build();
    }

    /**
     * If the credentials cannot be accessed for any reason this routine will call System.exit !!
     *
     * @return the GoogleCredentials object
     */
    public static GoogleCredentials getGoogleCredentials(String credentialsFilename){
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFilename))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        } catch(IOException nfx){
            System.err.println("Could not access the credentials file \"" + credentialsFilename + "\" or it doesn't exist.");
            System.exit(1);
        }
        return credentials;
    }
    public static void main(String []args){
        if (args.length < 4){
            System.err.println("Requires  4 arguments: credentials-file project region endpoint-id");
        }

        String credentialsFilename = args[0];
        String project = args[1];
        String region = args[2];
        String endpointId = args[3];
        float fraudDecisionThreshold = 0.5f;

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("Auth Request Pipeline");
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        GoogleCredentials credentials = PredictionPipeline.getGoogleCredentials(credentialsFilename);
        hz.getJet().newJob(PredictionPipeline.buildPipeline(project,region,endpointId, fraudDecisionThreshold, credentials));
    }
}
