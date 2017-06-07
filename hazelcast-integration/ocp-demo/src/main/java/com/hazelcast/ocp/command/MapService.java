package com.hazelcast.ocp.command;


public interface MapService {

    /**
     * Randomly insert key value pairs to {@link com.hazelcast.core.IMap}
     * @param count number of elements to put
     * @return new map size after put operation
     */
    int insert(int count);

    /**
     * @implNote currently only returns map size
     * @return statistical data regarding map.
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
}
