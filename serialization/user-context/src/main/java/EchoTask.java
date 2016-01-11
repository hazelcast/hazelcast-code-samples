import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;

public class EchoTask implements Runnable, Serializable, HazelcastInstanceAware {

    private final String msg;
    private transient HazelcastInstance hz;

    EchoTask(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        EchoService echoService = (EchoService) hz.getUserContext().get("echoService");
        echoService.echo(msg);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
    }
}
