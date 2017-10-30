package com.hazelcast.samples.querying;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Pipeline;
import com.hazelcast.projection.Projection;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.samples.querying.domain.LifeValue;
import com.hazelcast.samples.querying.domain.PersonKey;
import com.hazelcast.samples.querying.domain.PersonValue;
import com.hazelcast.samples.querying.jet.MyJoinPipeline;

/**
 * <P>
 * Implement 4 extra commands for Spring Shell. Another is in
 * {@link com.hazelcast.samples.querying.testdata.TestDataLoader
 * TestDataLoader}.
 * </P>
 *
 * <OL>
 * <LI><B>howard</B>
 * <P>
 * Search by partial key. Find all entries in the map with the last name
 * "Howard" in the key.
 * </P>
 * </LI>
 * <LI><B>howard2</B>
 * <P>
 * Search by partial key extended. Find all entries in the map with the last
 * name "Howard" in the key, but now only select certain fields from the key and
 * the value to be returned.
 * </P>
 * </LI>
 * <LI><B>join</B>
 * <P>
 * Join the birth and death maps to produce the life map.
 * </P>
 * </LI>
 * <LI><B>list</B>
 * <P>
 * Display the map content.
 * </P>
 * </LI>
 * <LI><B>location</B>
 * <P>
 * Display the location of each entry. Which partition it is in and which server
 * JVM is hosting that partition.
 * </P>
 * </LI>
 * <LI><B>longevity</B>
 * <P>
 * Find who lived the longest.
 * </P>
 * </LI>
 * </OL>
 */
@Component
public class MyCommands implements CommandMarker {

    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private JetInstance jetInstance;

    /**
     * <P>
     * Search <U>keys</U> but return <U>values</U> from the <I>key-value</I> store.
     * </P>
     * <P>
     * The only way this differs from a normal query is the key field is prefixed by
     * "{@code __key}" to indicate it is a field in the key not the value.
     * </P>
     * <P>
     * "{@code __key}" refers to the whole key.
     * </P>
     * <P>
     * "{@code __key.lastName}" refers to one field in the key.
     * </P>
     */
    @CliCommand(value = "howard", help = "Find all people with last name 'Howard'")
    @SuppressWarnings("unchecked")
    public String howard() {

        IMap<PersonKey, PersonValue> personMap = this.hazelcastInstance.getMap("person");

        if (personMap.isEmpty()) {
            return "Map is empty, run 'load' first";
        }

        Predicate<PersonKey, PersonValue> predicate = new SqlPredicate("__key.lastName = 'Howard'");

        System.out.printf("PREDICATE : '%s'%n", predicate);

        Collection<PersonValue> personValues = personMap.values(predicate);

        personValues.forEach(personValue -> System.out.printf("PERSON : '%s'%n", personValue));

        return String.format("[%d row%s]", personValues.size(), (personValues.size() == 1 ? "" : "s"));
    }

    /**
     * <P>
     * Enhance the previous command.
     * </P>
     * <P>
     * Running the previous command "{@code howard}" isn't that useful. Values are
     * displayed but we don't know for which key they apply.
     * </P>
     * <P>
     * We could search {@code map.entrySet(predicate)} and this would return us the
     * fields we want, but also others. Since we know the key must contain the last
     * name "Howard" it's inefficient to include it in the result.
     * </P>
     * <P>
     * Instead, use a projection to specify which fields we require.
     * </P>
     */
    @CliCommand(value = "howard2", help = "Same as 'howard' but with projection")
    @SuppressWarnings("unchecked")
    public String howard2() {

        IMap<PersonKey, PersonValue> personMap = this.hazelcastInstance.getMap("person");

        if (personMap.isEmpty()) {
            return "Map is empty, run 'load' first";
        }

        Predicate<PersonKey, PersonValue> predicate = new SqlPredicate("__key.lastName = 'Howard'");

        // One field from the value and one field from the key
        Projection<Entry<PersonKey, PersonValue>, Object[]> projection = Projections.multiAttribute("dateOfBirth",
                "__key.firstName");

        System.out.printf("PREDICATE : '%s'%n", predicate);
        System.out.printf("PROJECTION : '%s'%n", projection);

        Collection<Object[]> personFields = personMap.project(projection, predicate);

        personFields.forEach(personField -> System.out.printf("PERSON : FIELD 0 '%s' : FIELD 1 '%s'%n", personField[0],
                personField[1]));

        return String.format("[%d row%s]", personFields.size(), (personFields.size() == 1 ? "" : "s"));
    }

    /**
     * <P>
     * Use a Jet pipeline to join two maps into a third. Essentially, materialising
     * a view.
     * </P>
     */
    @CliCommand(value = "join", help = "Join birth with death to produce life")
    public String join() {

        IMap<PersonKey, PersonValue> personMap = this.hazelcastInstance.getMap("person");
        IMap<String, LifeValue> lifeMap = this.hazelcastInstance.getMap("life");

        if (personMap.isEmpty()) {
            return "Map is empty, run 'load' first";
        }

        // Remove old results
        lifeMap.clear();

        // Prepare the execution plan
        Pipeline pipeline = MyJoinPipeline.build();

        // Run the join
        System.out.println("Running : " + pipeline);
        this.jetInstance.newJob(pipeline).join();

        return "Done";
    }

    /**
     * <P>
     * List the contents of the maps.
     * </P>
     */
    @CliCommand(value = "list", help = "List the contents of the maps")
    public void list() {
        IMap<PersonKey, PersonValue> personMap = this.hazelcastInstance.getMap("person");
        IMap<String, LifeValue> lifeMap = this.hazelcastInstance.getMap("life");

        if (personMap.isEmpty()) {
            System.out.println("Map 'person' is empty, run 'load' first");
        }
        if (lifeMap.isEmpty()) {
            System.out.println("Map 'life' is empty, run 'join' first");
        }

        String[] mapNames = { "person", "deaths", "life" };

        for (String mapName : mapNames) {
            IMap<?, ?> map = this.hazelcastInstance.getMap(mapName);

            System.out.printf("MAP : '%s'%n", mapName);

            for (Object key : map.keySet()) {
                System.out.printf("  => '%s' : '%s'%n", key, map.get(key));
            }

            System.out.printf("[%d row%s]%n", map.size(), (map.size() == 1 ? "" : "s"));
        }
    }

    /**
     * <P>
     * For each entry in the <I>key-value</I> store, display which partition it is
     * in, and which server JVM is currently hosting this partition.
     * </P>
     * <P>
     * Try varying the number of JVMs in the cluster and rerunning. Entries stay in
     * the same partition as they were, but this partition may move.
     * </P>
     */
    @CliCommand(value = "location", help = "Display the location of each entry")
    public String location() {

        IMap<PersonKey, PersonValue> personMap = this.hazelcastInstance.getMap("person");

        if (personMap.isEmpty()) {
            return "Map is empty, run 'load' first";
        }

        Set<PersonKey> keySet = personMap.keySet();

        keySet.forEach(personKey -> {
            System.out.printf("PERSON : '%s' : PARTITION '%s'%n", personKey,
                    this.hazelcastInstance.getPartitionService().getPartition(personKey));
        });

        return String.format("[%d row%s]", keySet.size(), (keySet.size() == 1 ? "" : "s"));
    }

    /**
     * <P>
     * Use built-in aggregations to find the life record with the greatest age --
     * who lived the longest.
     * </P>
     * <P>
     * This uses the "age" field, which is derived by
     * {@link com.hazelcast.samples.querying.domain.LifeAgeValueExtractor
     * LifeAgeValueExtractor} but acts as if it was a field in the
     * {@link com.hazelcast.samples.querying.domain.LifeValue LifeValue} object.
     * </P>
     */
    @SuppressWarnings("rawtypes")
    @CliCommand(value = "longevity", help = "Who lived the longest")
    public String longevity() {

        IMap<String, LifeValue> lifeMap = this.hazelcastInstance.getMap("life");

        if (lifeMap.isEmpty()) {
            return "Map is empty, run 'join' first";
        }

        // Find max age
        int max = lifeMap.aggregate(Aggregators.integerMax("age"));

        System.out.printf("MAX AGE : '%d'%n", max);

        // Find those with that age
        Predicate predicate = new SqlPredicate("age = " + max);

        System.out.printf("PREDICATE : '%s'%n", predicate);

        Set<String> keySet = lifeMap.keySet(predicate);

        keySet.forEach(key -> {
            System.out.printf("  => '%s' : '%s'%n", key, lifeMap.get(key));
        });

        return String.format("[%d row%s]", keySet.size(), (keySet.size() == 1 ? "" : "s"));

    }

}
