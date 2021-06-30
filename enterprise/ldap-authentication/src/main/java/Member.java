import java.io.IOException;

import com.hazelcast.core.Hazelcast;

public class Member {

    public static void main(String[] args) throws IOException {
        Hazelcast.newHazelcastInstance();
    }
}
