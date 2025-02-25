package net.wrmay.jetdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class MachineEmulator implements  Runnable{
    private final String serialNum;

    private final SignalGenerator tempSignalGenerator;
    private final ObjectMapper objectMapper;

    private final CloseableHttpClient httpClient;
    private String targetURL;

    private int t;
    public MachineEmulator(CloseableHttpClient httpClient, String targetURL, String sn, boolean isHot, ObjectMapper objectMapper){
        System.out.println("Initializing machine emulator S/N: " + sn + " HOT: " + isHot);
        this.objectMapper = objectMapper;
        this.serialNum = sn;
        this.t = 0;

        if (isHot)
            tempSignalGenerator = new SignalGenerator(100f,3f,2f);
        else
            tempSignalGenerator = new SignalGenerator(95f, 0f, 1f);

        this.httpClient = httpClient;
        this.targetURL = targetURL;
    }

    @Override
    public void run() {
        MachineStatus status = new MachineStatus();
        status.setSerialNum(serialNum);
        status.setTimestamp(System.currentTimeMillis());
        status.setBitTemp(tempSignalGenerator.compute(t++));
        status.setBitRPM(10000);
        status.setBitPositionX(0);
        status.setBitBitPositionY(0);
        status.setBitPositionZ(0);

        String json;
        try {
            json = objectMapper.writeValueAsString(status);
            HttpPost post = new HttpPost(targetURL);
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = httpClient.execute(post)){
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299){
                    System.err.println("Server returned status code " + statusCode );
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        } catch(JsonProcessingException jmx){
            System.err.println("Error mapping status object to JSON: " + status);
        }
    }
}
