package com.hazelcast.samples.session.analysis;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.jet.core.processor.Processors;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.function.DistributedFunction;
import com.hazelcast.jet.function.DistributedFunctions;
import com.hazelcast.jet.function.DistributedPredicate;
import com.hazelcast.jet.pipeline.JournalInitialPosition;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.journal.EventJournalMapEvent;
import com.hazelcast.spi.serialization.SerializationService;
import com.hazelcast.web.SessionState;

/**
 * <p>Analyse the sequence of items being added to baskets.
 * Most of the work is in {@link #build()} which creates a
 * processing pipeline for Jet to run. A processing pipeline
 * is a sequence of steps to transform input into output
 * </p>
 */
public class SequenceAnalysis {

    private static final SerializationService SERIALIZATION_SERVICE =
            new DefaultSerializationServiceBuilder().build();

    private static final DistributedPredicate
        <EventJournalMapEvent<String, SessionState>>
            NO_SELECTION_FILTER = DistributedFunctions.alwaysTrue();
    private static final DistributedFunction
            <EventJournalMapEvent<String, SessionState>, EventJournalMapEvent<String, SessionState>>
        NO_PROJECTION_FILTER = DistributedFunctions.wholeItem();

    /**
     * <p>A six processing pipeline, starting from the journal of <u>changes</u>
     * to the "{@code jsessionid}" {@link com.hazelcast.core.IMap IMap} as input,
     * storing the resultant calculation in the "{@code sequence}"
     * {@link com.hazelcast.core.IMap IMap} as output.
     * </p>
     * <p>What we are trying to find is the sequence in which things are added
     * to shopping baskets in the online store, so we can answer questions such
     * as "</i>which item is usually added last ?</i>"
     * </p>
     * <p>The data derived answers this question. Data records such as
     * <pre>
     * Tuple2{1, Gloves} 3
     * Tuple2{1, Hats} 2
     * </pre>
     * tells us that "gloves" as the first item added to the basket has happened
     * 3 times and that "hats" has the first item has happened 2 times. Meaning,
     * gloves are added first more times than hats.
     * </p>
     *
     * <p>The method uses these six steps, run in order:
     * <pre>
     *              +------------+
     *              |1   IMap    |
     *              |"jsessionid"|
     *              |   Journal  |
     *              +------------+
     *                     |
     *                     |
     *                     |
     *              +------------+
     *              |2           |
     *              |   Filter   |
     *              |            |
     *              +------------+
     *                     |
     *                     |
     *                     |
     *              +------------+
     *              |3           |
     *              |  Reformat  |
     *              |            |
     *              +------------+
     *                     |
     *                     |
     *                     |
     *              +------------+
     *              |4           |
     *              |   Filter   |
     *              |            |
     *              +------------+
     *                     |
     *                     |
     *                     |
     *              +------------+
     *              |5           |
     *              |  Reformat  |
     *              |            |
     *              +------------+
     *                     |
     *                     |
     *                     |
     *              +------------+
     *              |6   IMap    |
     *              | "sequence" |
     *              | tally sink |
     *              +------------+
     * </pre>
     * <ol>
     * <li><b>Source</b>
     * <p>The source here is a map journal attached to the map that stores
     * the HTTP sessions. So what this gives us the history of session change -
     * sessions are created, updated several times, then may expire or be
     * removed once each.
     * </p>
     * <p>This stage can take a selection and projection, but for clarity
     * we chose to do them more explicitly as steps 2 and 3.
     * </p>
     * </li>
     * <li><b>Filter</b>
     * <p>Discard changes to the session that aren't updates (ie. creations and
     * deletions). These don't change the basket contents so are not of interest.
     * Although this doesn't remove many data records, it simplifies the next
     * stage as it doesn't have to worry that old or new value may be null.
     * </p>
     * </li>
     * <li><b>Reformat</b>
     * <p>Discard unwanted data, by selecting only the field we want from
     * the old and new map entry. That is, we care about the basket but
     * not the session id or user agent.
     * </p>
     * </li>
     * <li><b>Filter Again</b>
     * <p>Remove records where the number of types of things in the basket
     * does not change. We care if a hat is added to a basket that does
     * not have a hat. We do not care if a second hat is added to a basket
     * with a hat.
     * </p>
     * </li>
     * <li><b>Reformat Again</b>
     * <p>Once we have the old and new baskets, our interest is only in
     * what has been added. So we can simplify the data further, to
     * produce a pair of what was last added. For instance "{@code (3, Glove)}"
     * to record the third item added was a glove.
     * </p>
     * </li>
     * <li><b>Tally</b>
     * <p>Insert or merge the calculated item into the
     * {@link com.hazelcast.core.IMap Imap} named "{@code sequence}".
     * </p>
     * </li>
     * </ol>
     * </p>
     *
     * @return Processing to run in a Jet {@link com.hazelcast.jet.Job Job}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Pipeline build() {
        Pipeline pipeline = Pipeline.create();

        pipeline
        // (1)
        .drawFrom(Sources.mapJournal(Constants.IMAP_NAME_JSESSIONID,
            NO_SELECTION_FILTER,
            NO_PROJECTION_FILTER,
            JournalInitialPosition.START_FROM_OLDEST))
        // (2)
        .filter(eventJournalMapEvent -> eventJournalMapEvent.getType().equals(EntryEventType.UPDATED))
        // (3)
        .customTransform("beforeAndAfterBaskets", Processors.mapP(SequenceAnalysis::beforeAndAfterBaskets))
        // (4)
        .filter(tuple2 ->
                ((Tuple2<Map<String, Integer>, Map<String, Integer>>) tuple2).f0().size()
                < ((Tuple2<Map<String, Integer>, Map<String, Integer>>) tuple2).f1().size()
                )
        // (5)
        .customTransform("lastItem", Processors.mapP(SequenceAnalysis::lastItem))
        // (6)
        .drainTo(
               Sinks.mapWithEntryProcessor(
                Constants.IMAP_NAME_SEQUENCE,
                   DistributedFunctions.wholeItem(),
                    key -> ((EntryProcessor) new IncrementEntryProcessor())
             )
            )
        ;

        return pipeline;
    }

    /**
     * <p>A mapping function to reduce a {@link com.hazelcast.map.journal.EventJournalMapEvent
     * EventJournalMapEvent} down to parts we are interested in.
     * </p>
     * <p>At the point this is called, for {@code UPDATE} events only, we can be sure that
     * old and new values exist so don't bother to test for them being null.
     * </p>
     *
     * @param eventJournalMapEvent A change record for an {@link com.hazelcast.core.IMap IMap}
     * @return A pair of baskets, one each from before and after
     */
    private static Tuple2<Map<String, Integer>, Map<String, Integer>>
        beforeAndAfterBaskets(EventJournalMapEvent<String, SessionState> eventJournalMapEvent) {

        @SuppressWarnings("unchecked")
        Map<String, Integer> before
        = (Map<String, Integer>)
            SERIALIZATION_SERVICE.toObject(eventJournalMapEvent.getOldValue().getAttributes()
            .get(Constants.SESSION_ATTRIBUTE_BASKET));

        @SuppressWarnings("unchecked")
        Map<String, Integer> after
        = (Map<String, Integer>)
            SERIALIZATION_SERVICE.toObject(eventJournalMapEvent.getNewValue().getAttributes()
            .get(Constants.SESSION_ATTRIBUTE_BASKET));

        return Tuple2.tuple2(before, after);
    }

    /**
     * <p>Compare the before and after baskets, to determine what item was
     * added to the basket and when it was added. For example, the second
     * item added was a hat.
     * </p>
     * <p>Earlier filter processing allows us to optimise that the new
     * basket contains exactly one more entry than the old one.
     * </p>
     *
     * @param tuple2 A pair of baskets, one each from before and after
     * @return The item added to the basket
     */
    private static Tuple2<Integer, String>
        lastItem(Tuple2<Map<String, Integer>, Map<String, Integer>> tuple2) {

        Set<String> before = tuple2.f0().keySet();
        TreeSet<String> after = new TreeSet<>(tuple2.f1().keySet());

        after.removeAll(before);

        return Tuple2.tuple2(before.size() + 1, after.first());
    }

    /**
     * <p>Use an entry processor to make running totals visible in an
     * {@link com.hazelcast.map.IMap IMap}. This way Jet can drain
     * results into the map and keep the totals in step.
     * </p>
     */
    @SuppressWarnings("serial")
    static class IncrementEntryProcessor extends AbstractEntryProcessor<Tuple2<Integer, String>, Integer> {
        @Override
        public Integer process(Entry<Tuple2<Integer, String>, Integer> entry) {
                Integer oldValue = entry.getValue();
                Integer newValue = (oldValue == null ? 1 : oldValue + 1);
                return entry.setValue(newValue);
        }
    }
}
