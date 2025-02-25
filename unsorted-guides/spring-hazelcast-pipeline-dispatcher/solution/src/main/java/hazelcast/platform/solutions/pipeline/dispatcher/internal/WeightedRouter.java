package hazelcast.platform.solutions.pipeline.dispatcher.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.jet.datamodel.Tuple2;
import hazelcast.platform.solutions.pipeline.dispatcher.RequestRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeightedRouter implements RequestRouter {

    private final Random random;
    private final List<Tuple2<Float, String>> optionList;

    private final String serviceName;
    public WeightedRouter(String serviceName, String routerConfig){
        this.serviceName = serviceName;
        Map<String, Integer> versionMap;
        try {
            versionMap = new ObjectMapper().readValue(routerConfig, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);   // THROW
        }
        optionList = new ArrayList<>(versionMap.size());
        this.random = new Random();
        int totalWeight = 0;
        for (Map.Entry<String,Integer> t: versionMap.entrySet()) totalWeight += t.getValue();

        int w = 0;
        for(Map.Entry<String,Integer> t: versionMap.entrySet()){
            w += t.getValue();
            optionList.add(Tuple2.tuple2( (float) w/ (float) totalWeight, t.getKey()));
        }
    }

    @Override
    public String getRequestMapName() {
        float r = random.nextFloat();
        for(Tuple2<Float,String> option: optionList){
            if (r <= option.f0()) return serviceName + "_" + option.f1() + "_request"; //RETURN
        }
        throw new RuntimeException("Internal error in WeightedRouter");
    }
}
