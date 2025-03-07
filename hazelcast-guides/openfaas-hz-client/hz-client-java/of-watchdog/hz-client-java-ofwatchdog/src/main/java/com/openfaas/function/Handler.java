package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Random;

import static com.hazelcast.client.impl.spi.impl.discovery.HazelcastCloudDiscovery.CLOUD_URL_BASE_PROPERTY;
import static com.hazelcast.client.properties.ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN;
import static com.hazelcast.client.properties.ClientProperty.STATISTICS_ENABLED;


public class Handler extends com.openfaas.model.AbstractHandler {

    HazelcastInstance client;
    IMap<String, String> map;

    public Handler(){
        super();
        ClientConfig config = new ClientConfig();
        config.getConnectionStrategyConfig().getConnectionRetryConfig().setClusterConnectTimeoutMillis(Long.MAX_VALUE); 
        config.getNetworkConfig().addAddress("hz-hazelcast.default");
        this.client = HazelcastClient.newHazelcastClient(config);
        this.map = this.client.getMap("map");
        System.out.println("Connection Successful!");
    }

    public IResponse Handle(IRequest req) {
        Response res = new Response();
        Random random = new Random();
        int randomKey = random.nextInt(1_000_000);
        this.map.put("key-" + randomKey, "value-" + randomKey);
	    res.setBody(String.valueOf(this.map.size()));
	    return res;
    }
}
