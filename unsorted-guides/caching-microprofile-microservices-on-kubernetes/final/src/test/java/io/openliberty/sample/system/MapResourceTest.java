package io.openliberty.sample.system;

import static org.junit.Assert.assertEquals;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MapResourceTest {


    private static String clusterUrl;
    private static String getUrl;
    private static String putUrl;

    private Client client;
    private static Response response;


    @BeforeClass
    public static void oneTimeSetup() {
        String clusterIp = "localhost";//System.getProperty("cluster.ip");
        String nodePort = "31000";//System.getProperty("system.node.port");
        clusterUrl = "http://" + clusterIp + ":" + nodePort + "/application/map/";
        getUrl = clusterUrl + "/get?key=test_key";
        putUrl = clusterUrl + "/put?key=test_key&value=test_value";
    }

    @Before
    public void setup() {
        response = null;
        client = ClientBuilder.newBuilder()
                .hostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();
    }

    @After
    public void teardown() {
        client.close();
    }


    @Test(timeout = 60000)
    public void testCache() throws InterruptedException{
        response = this.getResponse(putUrl);
        this.assertResponse(putUrl, response,204);
        response = this.getResponse(getUrl);
        this.assertResponse(getUrl, response,200);

        String responseBody = response.readEntity(String.class);

        String firstPod = responseProperty(responseBody, 2);
        String firstValue = responseProperty(responseBody, 0);

        String secondPod;
        String secondValue;

        while(true){

            response = null;
            response = this.getResponse(getUrl);
            this.assertResponse(getUrl, response ,200);
            responseBody = response.readEntity(String.class);
            secondPod = responseProperty(responseBody,2);
            secondValue = responseProperty(responseBody,0);

            if( !secondPod.equals(firstPod)) {

                break;
            }
        }

        assertEquals(secondValue, firstValue);

        response.close();
    }


    private Response getResponse(String url) {
        return client
                .target(url)
                .request()
                .header("Authorization", "Basic Ym9iOmJvYnB3ZA==")
                .get();
    }


    private void assertResponse(String url, Response response, int code) {
        assertEquals("Incorrect response code from " + url, code, response.getStatus());
    }

    private String responseProperty(String responseBody, int order){
        String[] parsed = responseBody.split(" ");
        return parsed[order];
    }

}