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
import com.hazelcast.samples.querying.domain.LifeValue;
import com.hazelcast.samples.querying.domain.PersonKey;
import com.hazelcast.samples.querying.domain.PersonValue;

/**
 * <P>Construct a map by streaming and joining (in memory)
 * the contents of two other maps. What we are trying to
 * do is join the contents of the "{@code person}" map
 * with the "{@code deaths}" map.
 * </P>
 * <P>
 * In relational database terms, it would look a bit like this.
 * </P>
 * <PRE>
 * SELECT firstName, dateOfBirth, dateOfDeath
 *   FROM person, deaths
 *  WHERE person.firstName = deaths.key
 * </PRE>
 * <P>
 * In Jet, it looks more like this.
 * </P>
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
 *                        |6 Convert |
 *                        | to Entry |
 *                        +----------+
 *                              |
 *                              |
 *                        +----------+
 *                        |7 "Life"  |
 *                        |   IMap   |
 *                        +----------+
 * </PRE>
 * <P>There are seven parts to this joining pipeline, numbered in
 * the diagram above.
 * </P>
 * <OL>
 * <LI><P><B>{@code Person} map</B>
 * Read from {@link com.hazelcast.core.IMap IMap} named "{@code person}" and
 * stream this a series of map entries into the pipeline.
 * </P></LI>
 * <LI><P><B>Reformat</B>
 * Create a tuple of the only two fields we want from the "{@code person}" map.</P></LI>
 * <LI><P><B>{@code Deaths} map</B>
 * Same as for step 1, except the name is "{@code deaths}"</P></LI>
 * <LI><P><B>Reformat</B>
 * Create a tuple of the only two fields we want from the "{@code deaths}" map.</P></LI>
 * <LI><P><B>Join</B>
 * Join the output of stages 2 and 4 for matching key ({@code firstName}</P>
 * <P>The output of this stage is a pair of {@code (String, LocalDate), (String, LocalDate)}
 * <P>
 * </LI>
 * <LI><P><B>Reformat</B>
 * Convert the output of the previous stage into a {@code String} key and
 * pair of {@code LocalDate} for value</P></LI>
 * <LI><P><B>{@code Life} map</B>
 * Save the output from stage 4 into an {@link com.hazelcast.core.IMap IMap}</P></LI>
 * </OL> 
 */
public class MyJoinPipeline {

	public static Pipeline build() {
		Pipeline pipeline = Pipeline.create();
		
		// 1 - read a map
		ComputeStage<Entry<PersonKey, PersonValue>> stage1
			= pipeline.drawFrom(Sources.<PersonKey, PersonValue>readMap("person"));

		// 2 - simplify output from step 1, smaller to transmit
		ComputeStage<Tuple2<String, LocalDate>> stage2
			= stage1.map(entry -> Tuple2.tuple2(entry.getKey().getFirstName(), entry.getValue().getDateOfBirth()));

		// 3 - read another map
		ComputeStage<Entry<String, LocalDate>> stage3
			= pipeline.drawFrom(Sources.<String, LocalDate>readMap("deaths"));

		// 4 - simplify output from step 3, smaller to transmit
		ComputeStage<Tuple2<String, LocalDate>> stage4
			= stage3.map(entry -> Tuple2.tuple2(entry.getKey(), entry.getValue()));

		// 5 - join output from steps 2 and 4 using the clause provided (ie. on first name)
		ComputeStage<Tuple2<Tuple2<String, LocalDate>, Tuple2<String, LocalDate>>> stage5
			= stage2.hashJoin(stage4, MyJoinPipeline.getJoinClause());
		
		// 6 - create a map entry from step 5 output
		ComputeStage<Entry<String, LifeValue>> stage6 =
				stage5.map(trio -> {
					// Key is field 0
					String key = trio.f0().f0();
					// Value is fields 1 & 2
					LifeValue value = new LifeValue();
					value.setDateOfBirth(trio.f0().f1());
					value.setDateOfDeath(trio.f1().f1());
					// Create a Map.Entry
					return new SimpleImmutableEntry<>(key, value);
		});
		
		// 7 - save the map entry
		stage6.drainTo(Sinks.writeMap("life"));
		
		// Return the query execution plan
		return pipeline;
	}

	
	/**
	 * <P>How to join a stream of "{@code tuple2(firstname,date-of-birth)}" 
	 * with "{@code tuple2(firstname,date-of-death)}" to give
	 * "{@code tuple2(tuple2(firstname,date-of-birth),date-of-death)}"
	 * </P>
	 * <P>What this means:
	 * </P>
	 * <PRE>
	 * 	&lt;String, Tuple2&lt;String, LocalDate&gt;, Tuple2&lt;String, LocalDate&gt;, LocalDate&gt;
	 * </PRE>
	 * <OL>
	 * <LI>The key being used for the join is a {@code String}</LI>
	 * <LI>The first half of the join is a stream of {@code (String, LocalDate)}</LI>
	 * <LI>The second half of the join is a stream of {@code (String, LocalDate)}</LI>
	 * <LI>What is taken from the second stream to add to the first is a {@code (String, LocalDate)} pair</LI>
	 * </OL>
	 *  
	 * @return Take a pair, make a new pair containing the old pair and one field
	 */
	private static JoinClause<String, Tuple2<String, LocalDate>, Tuple2<String, LocalDate>, Tuple2<String, LocalDate>>
		getJoinClause() {

		// Get the key to match from the first tuple pair
		DistributedFunction<Tuple2<String, LocalDate>, String> leftKeyFn
		= Tuple2::f0;
		
		// Get the key to match from the second tuple pair
        DistributedFunction<Tuple2<String, LocalDate>, String> rightKeyFn
		= Tuple2::f0;
        
        // Match on full key
		return JoinClause.onKeys(leftKeyFn, rightKeyFn);
	}
}
