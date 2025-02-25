package hazelcast.platform.solutions.pipeline.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RoutingConfigTool {
    /**
     * usage: RoutingConfigTool [-h]
     *                            [--output OUTPUT]
     *                            [--input INPUT] {dump,load}
     * <p>
     * Inspect and maintain routing configuration
     * <p>
     * positional arguments:
     *   {dump,load}            specifies the desired action
     * <p>
     * named arguments:
     *   -h, --help             show this help message and exit
     *   --input INPUT          The JSON file containing the control data to load
     *   --output OUTPUT        The JSON file to which control data will be dumped
     * <p>
     *   Sample File Format
     *   {
     *   "serviceA": {
     *      "version1": 9,
     *      "versionX: 1
     *     },
     *   "serviceB": {
     *      "version1": 0,
     *      "versionX: 1
     *     }
     *   }
     *
     */
    public static void main(String []args){
        ArgumentParser parser = ArgumentParsers.newFor("RoutingConfigTool").build().defaultHelp(true)
                .description("Inspect and maintain routing configuration");

        parser.addArgument("action").choices("dump", "load").required(true).help("specifies the desired action");
        parser.addArgument("--input").type(String.class).required(false).help("The JSON file containing the control data to load");
        parser.addArgument("--output").type(String.class).required(false).help("The JSON file to which control data will be dumped");

        Namespace arguments = null;
        try {
            arguments = parser.parseArgs(args);
        } catch (ArgumentParserException x){
            parser.handleError(x);
            System.exit(1);
        }

        String action = arguments.getString("action");
        String inputFileName = arguments.getString("input");
        String outputFileName = arguments.getString("output");

        if (action.equals("load") && inputFileName == null){
            System.out.println("--input argument must be specified if the action is \"load\".");
            System.exit(1);
        }

        if (action.equals("dump") && outputFileName == null){
            System.out.println("--output argument must be specified if the action is \"dump\".");
            System.exit(1);
        }

        try {
            HazelcastInstance hz = HazelcastClient.newHazelcastClient();
            System.out.println("Connected");

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            IMap<String, String> remoteConfigMap = hz.getMap(PipelineDispatcherFactory.ROUTER_CONFIG_MAP);

            if (action.equals("load")) {
                Map<String, Map<String, String>> configMap = mapper.readValue(new File(inputFileName), Map.class);

                for (Map.Entry<String,Map<String,String>> entry: configMap.entrySet()){
                    String json = mapper.writeValueAsString(entry.getValue());
                    remoteConfigMap.put(entry.getKey(), json);
                }

                System.out.println("Loaded " + configMap.size() + " configuration entries from " + inputFileName);
            } else {
                Map<String, Map<String, String>> localMap = new HashMap<>();
                for (Map.Entry<String, String> entry : remoteConfigMap) {
                    Map<String, String> map = mapper.readValue(entry.getValue(), Map.class);
                    localMap.put(entry.getKey(), map);
                }

                mapper.writeValue(new File(outputFileName), localMap);
                System.out.println("Dumped " + localMap.size() + " configuration entries to " + outputFileName);
            }

            hz.shutdown();
        } catch(Exception rx){
            System.out.println("An error occurred. Program will exit.");
            rx.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
