package com.hzsamples.automl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.hazelcast.jet.datamodel.Tuple2.tuple2;

public class PredictionPipeline {
    public static Pipeline buildPipeline(String modelProject, String modelLocation, String modelEndpointId, float fraudConfidenceThreshold, GoogleCredentials credentials){
        Pipeline result = Pipeline.create();

        // read input authorization requests serialized as byte []

        // deserialize the input into a java POJO

        // extract the fields of interest from the POJO and format as a protobuf Struct as required for the Vertex AI endpoint

        // create a custom prediction service to call a Vertex AI endpoint in Google Cloud
        // this is a thin wrapper around the Vertex AI java API
        ServiceFactory<?, AutoMLTabularPredictionClient> predictionService =
                ServiceFactories.nonSharedService(c -> new AutoMLTabularPredictionClient(
                        modelProject,
                        modelLocation,
                        modelEndpointId,
                        credentials)).toNonCooperative();

        // use the custom prediction service to obtain a PredictionResponse, wrap the response in a helper class for ease of use

        // filter out predictions that yielded an exception for any reason

        // based on the confidence scores in the prediction response, make a fraud prediction

        // output the decision

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
