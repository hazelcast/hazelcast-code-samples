package com.hazelcast.samples.json.jsongrid;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.core.MapLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>A {@link com.hazelcast.core.MapLoader MapLoader} provides a way to get data
 * in to a Hazelcast {@link com.hazelcast.core.IMap} from an external
 * store. Other ways exist.
 * </p>
 * <p>In this case the loader is generic, take the name of an SQL table as
 * a  constuctor parameter, and using a utliity class to read a row from
 * that table and convert into JSON.
 * </p>
 * <p>As far as Hazelcast is concerned, there are three main ways this is
 * used:
 * </p>
 * <ol>
 * <li>
 * <p><b>Iterable<K> loadAllKeys()</b>
 * </p>
 * <p>This is called first. It locals all primary keys in the database
 * that we wish to load. This doesn't have to be all primary keys in
 * the database, but in this case it is.
 * </p>
 * <p>This list is split across the available Hazelcast processes for
 * each to load it's allocated subset.
 * </p>
 * <p>More correctly, the list is split across parttions in the
 * Hazelcast processes.
 * </p>
 * <p>This method can return an empty collection if we don't want
 * to load any data at start-up.
 * </p>
 * </li>
 * <li>
 * <p><b>Map<K,V> loadAll(Collection<K>)</b>
 * </p>
 * <p>This is called several times, each with some of the keys
 * found by the previous method ("{@code loadAllKeys()}" until
 * have been used as input.
 * </p>
 * <p>The job of this method is to load that block of primary
 * keys (ie. some of the rows in the database).
 * </p>
 * <p>This method is nod towards efficiency. If there's a good
 * way to load of block of records from the database into the
 * Hazelcast map, the implementation can exploit it in this
 * method. If not, it can iterate through loading each from
 * the database individually.
 * </p>
 * <p>As it happens, we could load a block of records from
 * <a href="http://hsqldb.org/">HSQLDB</a>, but to keep this
 * example simple, we just do it iteratively.
 * </p>
 * </li>
 * <li>
 * <p><b>V load(K)</b>
 * </p>
 * <p>This method is called once per primary key to try to
 * load the associated value. It may return null if the
 * primary key no longer exists.
 * </p>
 * </li>
 */
@Component
@Slf4j
@Scope("prototype")
public class MyJsonMapLoader implements MapLoader<Integer, HazelcastJsonValue> {

    private String tableName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MyJsonMapLoader(String arg0) {
        this.tableName = arg0;
    }

    /**
     * <p>Use the generic database utility to read a single row
     * from the database, using the primary key provided, and
     * return it as a JSON object.
     * </p>
     *
     * @param Key The database row to look for
     * @return May be null if key not found
     */
    @Override
    public HazelcastJsonValue load(Integer key) {
        log.debug("load({})", key);

        HazelcastJsonValue hazelcastJsonValue =
                MyDbUtil.rowToJson(this.jdbcTemplate, this.tableName, key);

        log.info("load({}) -> {}", key, hazelcastJsonValue);

        return hazelcastJsonValue;
    }

    /**
     * <p>Load the specified subset of keys into the current server process.
     * Here we just iterate across loading each individually, however we
     * could use a database select query with a set of keys to do the
     * same if this is more efficient.
     * </p>
     * <p>Something like:
     * <pre>
     * SELECT * FROM table WHERE id IN ( 1, 2, 3);
     * </pre>
     * </p>
     */
    @Override
    public Map<Integer, HazelcastJsonValue> loadAll(Collection<Integer> keys) {
        log.debug("loadAll({})", keys);

        return keys
        .stream()
        .map(key -> new SimpleImmutableEntry<>(key, this.load(key)))
        .filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    /**
     * <p>This method determine which keys from the database table that
     * we wish to load, in this case it is all of them.
     * </p>
     * <p>Push all the work to a generic database query that takes
     * the table name and returns all primary keys, assuming that
     * these are integers.
     * </p>
     */
    @Override
    public Iterable<Integer> loadAllKeys() {
        log.debug("loadAllKeys()");

        Collection<Integer> ids = MyDbUtil.findIds(this.jdbcTemplate, this.tableName);

        log.info("loadAllKeys() -> {}", (ids == null ? ids : ids.size()));

        return ids;
    }
}
