package com.hazelcast.ocp.command;

import com.hazelcast.ocp.entryprocessor.Position;

public interface MapService {

    /**
     * Randomly insert key value pairs to {@link com.hazelcast.core.IMap}
     *
     * @param count number of elements to put
     * @return new map size after put operation
     */
    int insert(int count);

    /**
     * @return statistical data regarding map.
     * @implNote currently only returns map size
     */
    int stats();

    /**
     * runs get, put tests with random data and operation in a thread pool
     */
    void runAutoPilot();

    /**
     * removes all elements from underlying distributed {@link com.hazelcast.core.IMap} implementation
     */
    void clear();

    /**
     * randomly inserts {@link Position} instances to underlying {@link com.hazelcast.core.IMap}
     */
    int insertPositions(int keyCount);

    /**
     * Calculates distances that are inserted with {@link MapService::insertPositions} against a random point
     * to demonstrate {@link com.hazelcast.map.EntryProcessor} semantics.
     */
    long processDistances(Position position);
}
