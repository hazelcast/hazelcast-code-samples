import com.hazelcast.console.ConsoleApp;

public class HazelcastMember {

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.local.localAddress", "127.0.0.1");
        ConsoleApp.main(args);
    }
}
