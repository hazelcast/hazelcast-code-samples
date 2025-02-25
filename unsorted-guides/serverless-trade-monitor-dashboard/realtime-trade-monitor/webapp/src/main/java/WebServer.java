import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.query.impl.predicates.EqualPredicate;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WebServer {

    private static final Map<String, WsContext> sessions = new ConcurrentHashMap<>();
    private static final Map<String, List<WsContext>> symbolsToBeUpdated = new ConcurrentHashMap<>();
    private static final HazelcastInstance client = HazelcastClient.newHazelcastClient();


    public static void main(String[] args) {
        IMap<String, Tuple3<Long, Long, Integer>> results = client.getMap("query1_Results");
        IMap<String, HazelcastJsonValue> trades = client.getMap("trades");
        IMap<String, String> symbols = client.getMap("symbols");

        trades.addEntryListener(new TradeRecordsListener(), true);

        Javalin app = Javalin.create().start(9000);
        app.config
                .addStaticFiles("/app")                                     // The ReactJS application
                .addSinglePageRoot("/", "/app/index.html");   // Catch-all route for the single-page application

        app.ws("/trades", wsHandler -> {
            wsHandler.onConnect(ctx -> {
                String sessionId = ctx.getSessionId();
                System.out.println("Starting the session -> " + sessionId);
                sessions.put(sessionId, ctx);
            });

            wsHandler.onClose(ctx -> {
                String sessionId = ctx.getSessionId();
                System.out.println("Closing the session -> " + sessionId);
                sessions.remove(sessionId, ctx);
                for (Entry<String, List<WsContext>> entry : symbolsToBeUpdated.entrySet()) {
                    List<WsContext> contexts = entry.getValue();
                    contexts.removeIf(context -> context.getSessionId().equals(sessionId));
                }
            });

            wsHandler.onMessage(ctx -> {
                String sessionId = ctx.getSessionId();
                String message = ctx.message();
                WsContext session = sessions.get(sessionId);

                if ("LOAD_SYMBOLS".equals(message)) {
                    JSONObject jsonObject = new JSONObject();
                    Map<String, String> allSymbols = symbols.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                    results.forEach((key, value) -> {
                        jsonObject.append("symbols", new JSONObject()
                                .put("name", allSymbols.get(key))
                                .put("symbol", key)
                                .put("count", value.f0())
                                .put("volume", priceToString(value.f1()))
                                .put("price", value.f2())
                        );
                    });
                    session.send(jsonObject.toString());
                } else if (message.startsWith("DRILL_SYMBOL")) {
                    String symbol = message.split(" ")[1];
                    System.out.println("Session -> " + sessionId + " requested symbol -> " + symbol);
                    symbolsToBeUpdated.compute(symbol, (k, v) -> {
                        if (v == null) {
                            v = new ArrayList<>();
                        }
                        v.add(session);
                        return v;
                    });

                    JSONObject jsonObject = new JSONObject();
                    Collection<HazelcastJsonValue> records = trades.values(new EqualPredicate("symbol", symbol));
                    records.forEach(trade -> {
                        String tradeJson = trade.toString();
                        jsonObject.put("symbol", symbol);
                        jsonObject.append("data", new JSONObject(tradeJson));
                    });
                    session.send(jsonObject.toString());
                }
            });
        });
    }

    private static class TradeRecordsListener implements EntryAddedListener<String, HazelcastJsonValue> {

        @Override
        public void entryAdded(EntryEvent<String, HazelcastJsonValue> event) {
            JSONObject tradeJson = new JSONObject(event.getValue().toString());
            String symbol = tradeJson.getString("symbol");
            List<WsContext> contexts = symbolsToBeUpdated.get(symbol);
            if (contexts != null && !contexts.isEmpty()) {
                System.out.println("Broadcasting update on = " + symbol);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("symbol", symbol);
                jsonObject.append("data", tradeJson);
                String message = jsonObject.toString();
                for (WsContext context : contexts) {
                    context.send(message);
                }
            }
        }
    }

    private static String priceToString(long price) {
        return String.format("$%,.2f", price / 100.0d);
    }
}
