/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.samples.sql.hazdb;

import java.util.Objects;

import com.hazelcast.function.PredicateEx;
import com.hazelcast.jet.Observable;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.JournalInitialPosition;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.map.EventJournalMapEvent;

/**
 * <p>
 * Apply a projection/selection to the journal of events on the selected map.
 * Projection reformats. Selection is a no-op.
 * </p>
 */
public class CdcPipeline {

    /**
     * <p>
     * Process changes to the nominated map. New value may be null if delete. Old
     * value (not used) may be null if create. Both present for create.
     * </p>
     *
     * @param observable Null allowed, if no clientside logging needed
     */
    @SuppressWarnings("rawtypes")
    public static Pipeline build(String mapName, Observable<Tuple3<String, String, String>> observable) {
        Pipeline pipeline = Pipeline.create();

        StreamStage<Tuple3<String, String, String>> events = pipeline.readFrom(Sources.mapJournal(mapName,
                JournalInitialPosition.START_FROM_CURRENT, (EventJournalMapEvent eventJournalMapEvent) -> {
                    // Filter out no-ops
                    String before = Objects.toString(eventJournalMapEvent.getOldValue());
                    String after = Objects.toString(eventJournalMapEvent.getNewValue());
                    if (before.equals(after)) {
                        return null;
                    }

                    return Tuple3.tuple3(mapName, eventJournalMapEvent.getType().toString(),
                            Utils.makeText(eventJournalMapEvent.getKey()) + ","
                                    + Utils.makeText(eventJournalMapEvent.getNewValue()));
                }, PredicateEx.alwaysTrue())).withoutTimestamps();

        // Log serverside
        events.writeTo(Sinks.logger());

        // Log clientside
        if (observable != null) {
            events.writeTo(Sinks.observable(observable));
        }

        return pipeline;
    }

}
