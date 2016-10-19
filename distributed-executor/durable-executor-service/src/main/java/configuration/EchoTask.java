package configuration;

import java.io.Serializable;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

public class EchoTask implements Runnable, Serializable {

    private final String msg;

    EchoTask(String msg) {
        this.msg = msg;
    }

    public void run() {
        sleepSeconds(5);

        System.out.println("Echo: " + msg);
    }
}
