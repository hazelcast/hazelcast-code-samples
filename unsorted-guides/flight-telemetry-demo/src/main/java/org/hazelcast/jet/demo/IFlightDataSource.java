package org.hazelcast.jet.demo;

import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.json.JsonValue;
import com.hazelcast.jet.pipeline.SourceBuilder;

import java.io.IOException;

public interface IFlightDataSource {

    void fillBuffer(SourceBuilder.TimestampedSourceBuffer<Aircraft> buffer) throws IOException;

    JsonObject pollForAircraft() throws IOException ;

    static Aircraft parseAircraft(JsonValue ac) {
        Aircraft aircraft = new Aircraft();
        aircraft.fromJson(ac.asObject());
        return aircraft;
    }
}
