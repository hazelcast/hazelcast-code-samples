package com.hazelcast.samples.querying.jet;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import com.hazelcast.jet.ComputeStage;
import com.hazelcast.jet.JoinClause;
import com.hazelcast.jet.Pipeline;
import com.hazelcast.jet.Sinks;
import com.hazelcast.jet.Sources;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.function.DistributedFunction;
import com.hazelcast.jet.function.DistributedFunctions;
import com.hazelcast.samples.querying.domain.LifeValue;
import com.hazelcast.samples.querying.domain.PersonKey;
import com.hazelcast.samples.querying.domain.PersonValue;

/**
 * <P>
 * Construct a map by streaming and joining (in memory) the contents of two
 * other maps. What we are trying to do is join the contents of the
 * "{@code person}" map with the "{@code deaths}" map.
 * </P>
 * <P>
 * In relational database terms, it would look a bit like this.
 * </P>
 *
 * <PRE>
 * SELECT firstName, dateOfBirth, dateOfDeath
 *   FROM person, deaths
 *  WHERE person.firstName = deaths.key
 * </PRE>
 * <P>
 * In Jet, it looks more like this.
 * </P>
 *
 * <PRE>
 *          +----------+                +----------+
 *          |1 "Person"|                |3 "Deaths"|
 *          |  IMap    |                |  IMap    |
 *          +----------+                +----------+
 *               |                            |
 *               |                            |
 *          +----------+                +----------+
 *          |3 "Person"|                |4 "Deaths"|
 *          | to tuple |                | to tuple |
 *          +----------+                +----------+
 *                      \              /
 *                       \            /
 *                       +------------+
 *                       |5  Join     |
 *                       |on firstName|
 *                       +------------+
 *                              |
 *                              |
 *                        +----------+
 *                        |6 Filter  |
 *                        | unmatched|
 *                        +----------+
 *                              |
 *                              |
 *                        +----------+
 *                        |7 Convert |
 *                        | to Entry |
 *                        +----------+
 *                              |
 *                              |
 *                        +----------+
 *                        |8 "Life"  |
 *                        |   IMap   |
 *                        +----------+
 * </PRE>
 * <P>
 * There are eight parts to this joining pipeline, numbered in the diagram
 * above.
 * </P>
 * <OL>
 * <LI>
 * <P>
 * <B>{@code Person} map</B> Read from {@link com.hazelcast.core.IMap IMap}
 * named "{@code person}" and stream this a series of map entries into the
 * pipeline.
 * </P>
 * </LI>
 * <LI>
 * <P>
 * <B>Reformat</B> Create a tuple of the only two fields we want from the
 * "{@code person}" map.
 * </P>
 * </LI>
 * <LI>
 * <P>
 * <B>{@code Deaths} map</B> Same as for step 1, except the name is
 * "{@code deaths}"
 * </P>
 * </LI>
 * <LI>
 * <P>
 * <B>Reformat</B> Create a tuple of the only two fields we want from the
 * "{@code deaths}" map.
 * </P>
 * </LI>
 * <LI>
 * <P>
 * <B>Join</B> Join the output of stages 2 and 4 for matching key
 * ({@code firstName}
 * </P>
 * <P>
 * The output of this stage is a pair of
 * {@code (String, LocalDate), (String, LocalDate)}
 * <P>
 * </LI>
 * <LI>
 * <P>
 * <B>Filter</B> Remove items from the join with only dates of birth, no dates
 * of death.
 * </P>
 * </LI>
 * <LI>
 * <P>
 * <B>Reformat</B> Convert the output of the previous stage into a
 * {@code String} key and pair of {@code LocalDate} for value
 * </P>
 * </LI>
 * <LI>
 * <P>
 * <B>{@code Life} map</B> Save the output from stage 4 into an
 * {@link com.hazelcast.core.IMap IMap}
 * </P>
 * </LI>
 * </OL>
 */
public class MyJoinPipeline {

    public static Pipeline build() {
        Pipeline pipeline = Pipeline.create();

        // 1 - read a map
        ComputeStage<Entry<PersonKey, PersonValue>> stage1 = pipeline
                .drawFrom(Sources.<PersonKey, PersonValue>map("person"));

        // 2 - simplify output from step 1, smaller to transmit
        ComputeStage<Tuple2<String, LocalDate>> stage2 = stage1
                .map(entry -> Tuple2.tuple2(entry.getKey().getFirstName(), entry.getValue().getDateOfBirth()));

        // 3 - read another map
        ComputeStage<Entry<String, LocalDate>> stage3 = pipeline.drawFrom(Sources.<String, LocalDate>map("deaths"));

        // 4 - simplify output from step 3, smaller to transmit
        ComputeStage<Tuple2<String, LocalDate>> stage4 = stage3
                .map(entry -> Tuple2.tuple2(entry.getKey(), entry.getValue()));

        // 5a - [optional] wrap existing entryKey() distributed function with types
        @SuppressWarnings({ "unchecked", "rawtypes" })
        DistributedFunction<Tuple2<String, LocalDate>, String> firstName =
                (DistributedFunction<Tuple2<String, LocalDate>, String>)
                (DistributedFunction) DistributedFunctions
                .entryKey();

        // 5b - join output from steps 2 and 4 (Tuple2 are map entries) on key
        ComputeStage<Tuple2<Tuple2<String, LocalDate>, LocalDate>> stage5 = stage2.hashJoin(stage4,
                JoinClause.joinMapEntries(firstName));

        // 6 - filter out unjoined
        ComputeStage<Tuple2<Tuple2<String, LocalDate>, LocalDate>> stage6 = stage5
                .filter(tuple2 -> tuple2.f1() != null);

        // 7 - create a map entry from step 6 output
        ComputeStage<Entry<String, LifeValue>> stage7 = stage6.map(trio -> {
            // Tuple2<Tuple2< key, date-of-birth>, date-of-death>
            String key = trio.f0().f0();
            LocalDate dob = trio.f0().f1();
            LocalDate dod = trio.f1();

            LifeValue value = new LifeValue();
            value.setDateOfBirth(dob);
            value.setDateOfDeath(dod);

            // Create a Map.Entry
            return new SimpleImmutableEntry<>(key, value);
        });

        // 8 - save the map entry
        stage7.drainTo(Sinks.map("life"));

        // Return the query execution plan
        return pipeline;
    }

}
