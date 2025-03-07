package hazelcast.platform.solutions.pipeline.dispatcher.internal;

import hazelcast.platform.solutions.pipeline.dispatcher.RequestRouter;

import java.util.Random;

public class MultiVersionRequestRouter implements RequestRouter {

    private final Random rand;
    private final MultiVersionRequestRouterConfig config;
    private final String name;

    public MultiVersionRequestRouter(String name, MultiVersionRequestRouterConfig config){
        rand = new Random();
        this.config = config;
        this.name = name;
    }

    @Override
    public String getRequestMapName() {
        String version = config.getVersion(rand.nextFloat());
        return name + "_" + version + "_request";
    }
}
