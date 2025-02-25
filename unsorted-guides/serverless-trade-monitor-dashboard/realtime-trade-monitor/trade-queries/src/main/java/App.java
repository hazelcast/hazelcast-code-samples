import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class App {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Available commands:");
            System.out.println(" load-symbols");
            System.out.println(" ingest-trades <bootstrap servers>");
            System.out.println(" aggregate-query <bootstrap servers>");
            System.out.println(" benchmark-index");
            System.out.println(" benchmark-latency");
            System.exit(1);
        }

        String command = args[0];

        HazelcastInstance hzInstance = HazelcastClient.newHazelcastClient();;
        try {
            if (command.equals("load-symbols")) {
                LoadSymbols.loadSymbols(hzInstance);
            } else if (command.equals("ingest-trades")) {
                IngestTrades.ingestTrades(hzInstance, args[1]);
            } else if (command.equals("aggregate-query")) {
                AggregateQuery.aggregateQuery(hzInstance, args[1]);
            } else if (command.equals("benchmark-index")) {
                Benchmark.benchmark(hzInstance);
            } else if (command.equals("benchmark-latency")) {
                BenchmarkLatency.benchmark(hzInstance);
            }
        } finally {
            hzInstance.shutdown();
        }
    }
}
