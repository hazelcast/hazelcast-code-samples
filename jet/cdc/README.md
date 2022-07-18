This folder contains examples for Hazelcast Jet CDC feature.

There are 3 examples includes:
- `com.hazelcast.samples.jet.cdc.Cache` is the most basic, which shows
    how to read from CDC source and write to IMap using CDC Sink

- `com.hazelcast.samples.jet.cdc.CdcRealTimeAnalysisDemo` shows how Hazelcast
    can enable real-time capabilities in applications. It reads from
    a CDC source, then aggregates the streaming data and saves it into
    an IMap. Later, this IMap may be read using Hazelcast SQL.

- `com.hazelcast.samples.jet.cdc.CdcRealTimeAnalysisWithParallelSnapshotDemo`
    is the same as previous example, but it adds reading from JDBC source
    in parallel to CDC, so that initial snapshot is done quicker.

Requirements to build and run examples:
- JDK 17 or higher installed
- Docker installed