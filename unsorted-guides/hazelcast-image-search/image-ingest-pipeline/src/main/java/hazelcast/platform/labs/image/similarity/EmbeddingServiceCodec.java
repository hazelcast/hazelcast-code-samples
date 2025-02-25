package hazelcast.platform.labs.image.similarity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.jet.datamodel.Tuple2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * The python service we use for calculating embeddings is limited to a single
 * String for input and another for output.  This class is responsible for turning the output from the
 * python service into Java events that are consumable by the rest of the Pipeline
 */
public class EmbeddingServiceCodec {
    private final Logger log = LogManager.getLogger();

    // safe for concurrent use
    private final ObjectMapper mapper;

    public EmbeddingServiceCodec(){
        this.mapper = new ObjectMapper();
    }

    /*
     * The output from the python service is json encoded mapping with the following possible attributes:
     *
     * "exception"   The value is a message describing the error.
     *
     * "metadata"    The value is just the name of the file.  The python service just passes this
     *               through from it's input.
     *
     * "vector"      The embedding, as a json list of numbers
     *
     * If "exception" is present it means the embedding failed.  In this case, an error message is logged and
     * null is returned.
     *
     * If "exception" is not present then the contents of "vector" are translated into an array of floats
     * and a (String filename, float []vector) 2-tuple is returned.
     */
    public Tuple2<String, float[]> decodeOutput(String json) throws JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        if (root.has("exception")){
            log.error(root.get("exception").asText());
            return null;  // RETURN
        }

        String filename = root.get("metadata").asText();

        float []vector = new float[root.get("vector").size()];
        int i=0;
        for (JsonNode node : root.get("vector")) {
            vector[i++] = (float) node.asDouble();
        }
        return Tuple2.tuple2(filename, vector);
    }

}
