package hazelcast.platform.labs.payments;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import hazelcast.platform.labs.payments.domain.Card;
import hazelcast.platform.labs.payments.domain.FakeData;
import hazelcast.platform.labs.payments.domain.Names;

import java.util.HashMap;
import java.util.Map;

/**
 * Expects the following environment variables
 * <p>
 * CARD_COUNT The number of machines credit cards to load
 *
 */
public class RefdataLoader {
    private static final String CARD_COUNT_PROP = "CARD_COUNT";

    private static final String CARD_MAPPING_SQL =
            "CREATE OR REPLACE MAPPING cards (" +
                    "cardNumber VARCHAR, " +
                    "creditLimitDollars INTEGER, " +
                    "authorizedDollars INTEGER, " +
                    "locked BOOLEAN" +
            ") Type IMap " +
            "OPTIONS ( " +
                "'keyFormat' = 'varchar', " +
                "'valueFormat' = 'compact' ," +
                "'valueCompactTypeName' = 'hazelcast.platform.labs.payments.domain.Card')";

    private static int cardCount;

    private static String getRequiredProp(String propName){
        String prop = System.getenv(propName);
        if (prop == null){
            System.err.println("The " + propName + " property must be set");
            System.exit(1);
        }
        return prop;
    }

    private static void configure(){
        String temp = getRequiredProp(CARD_COUNT_PROP);
        try {
            cardCount = Integer.parseInt(temp);
        } catch(NumberFormatException nfx){
            System.err.println("Could not parse " + temp + " as an integer");
            System.exit(1);
        }

        if (cardCount < 1 || cardCount > 10000000){
            System.err.println("Card count must be between 1 and 10,000,000 inclusive");
            System.exit(1);
        }
    }

    private static void doSQLMappings(HazelcastInstance hzClient){
            hzClient.getSql().execute(CARD_MAPPING_SQL);
        System.out.println("Initialized SQL Mappings");
    }

    public static void main(String []args){
        configure();

        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();

        doSQLMappings(hzClient);

        IMap<String, Card> cardMap = hzClient.getMap(Names.CARD_MAP_NAME);
        IMap<String, String> systemActivitiesMap = hzClient.getMap(Names.SYSTEM_ACTIVITIES_MAP_NAME);

        systemActivitiesMap.put("LOADER_STATUS","STARTED");

        int existingEntries = cardMap.size();
        int toLoad = cardCount - existingEntries;

        if (toLoad <= 0){
            System.out.println("" + existingEntries + " cards are already present");
        } else {
            Map<String, Card> batch = new HashMap<>();
            for(int i=0; i < toLoad; ++i){
                Card c = FakeData.card();
                batch.put(c.getCardNumber(), c);
                int BATCH_SIZE = 1000;
                if (batch.size() == BATCH_SIZE){
                    cardMap.putAll(batch);
                    batch.clear();
                }
            }

            if (batch.size() > 0) cardMap.putAll(batch);

            if (cardCount == toLoad)
                System.out.println("Loaded " + cardCount + " cards");
            else
                System.out.println("Loaded " + toLoad + " cards bringing the total to " + cardCount);
        }
        systemActivitiesMap.put("LOADER_STATUS","FINISHED");
        hzClient.shutdown();
    }
}
