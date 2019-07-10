package com.hazelcast.samples.json.jsongrid;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>Initialise the Hazelcast server by referencing
 * the {@link com.hazelcast.core.IMap IMap} instances.
 * </p>
 * <p>If this is the second, third, etc server in the
 * cluster the maps will already have been created (by
 * the first server) and this is a "{@code no-op}".
 * </p>
 * <p>If this is the first server, we access every
 * one of a set of maps, and this access triggers their
 * creation, which in turn triggers the map loaders
 * to pull in data from the database.
 * </p>
 * <p>The list of maps comes from the database, each
 * database table becomes a map of the same name.
 * </p>
 * <p><i>So...</i> our tables are found by inspecting
 * the database catalog, and the map loaders load
 * each table into a map with the same name.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationInitializer implements CommandLineRunner {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * <p>Each table in the database is one map in
     * Hazelcast (one {@link com.hazelcast.core.IMap IMap}).
     * </p>
     * <p>Find the table names and access the maps of the
     * same names, which forces their creation if they
     * don't exist and data loading actions.
     * </p>
     * <p>Print the first value from each to prove data
     * is there, whether loaded now or already loaded.
     * </p>
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void run(String... args) throws Exception {
        this.findTableNames().stream().forEach(name -> {
            log.info("this.hazelcastInstance.getMap(\"{}\")", name);
            IMap<Comparable, ?> iMap = this.hazelcastInstance.getMap(name);

            TreeSet<?> keys = new TreeSet<>(iMap.keySet());

            if (keys.size() == 0) {
                log.error("Map '{}' empty", name);
            } else {
                Object key = keys.first();
                Object value = iMap.get(key);

                log.info(" --> '{}'.get('{}')=='{}'",
                        iMap.getName(), key, value);
            }
        });
    }

    /**
     * <p>We know the table names, "{@code potus}" and "{@code vpotus}"
     * as we created them. This is just a cleverer way to find them.
     * </p>
     *
     * @return A sorted set of table names
     * @throws Exception If none found
     */
    private Set<String> findTableNames() throws Exception {
        Set<String> names = new TreeSet<>();

        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'";

        try {
            List<Map<String, Object>> resultSet = this.jdbcTemplate.queryForList(sql);

            for (int i = 0; i < resultSet.size(); i++) {
                Map<String, Object> map = resultSet.get(i);

                map.keySet().stream()
                .filter(key -> key.equalsIgnoreCase("table_name"))
                .map(key -> map.get(key).toString())
                .forEach(value -> names.add(value));
            }

        } catch (Exception e) {
            log.error(sql, e);
        }

        if (names.size() == 0) {
            throw new RuntimeException("No results for: " + sql);
        }

        return names;
    }
}
