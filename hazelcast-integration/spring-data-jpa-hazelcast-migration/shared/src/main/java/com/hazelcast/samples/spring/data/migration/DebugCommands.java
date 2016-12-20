package com.hazelcast.samples.spring.data.migration;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Debug diagnostic print of database and Hazelcast content.
 */
@Component
@Slf4j
public class DebugCommands implements CommandMarker {

    @Autowired(required = false)
    private HazelcastInstance hazelcastInstance;
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Show what is in the database. Would be cleaner with lambdas.
     */
    @CliCommand(value = "debugDB", help = "List the database content")
    public void debugDB() {
        if (this.jdbcTemplate == null) {
            log.info("JDBC Template is null");
            return;
        }

        for (String tableName : new String[]{"noun", "verb"}) {
            log.info("");
            try {
                List<Map<String, Object>> resultSet = this.jdbcTemplate.queryForList("SELECT * FROM " + tableName);

                log.info("Table '{}'", tableName);

                for (int i = 0; i < resultSet.size(); i++) {
                    Map<String, Object> map = resultSet.get(i);

                    log.info(" -> Row {}", i);
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        log.info("     -> {}=='{}'", entry.getKey().toLowerCase(), entry.getValue());
                    }
                }
                log.info("[{} row{}]", resultSet.size(), (resultSet.size() == 1 ? "" : "s"));
            } catch (Exception e) {
                log.error(tableName, e.getMessage());
            }
        }

        log.info("");
    }

    /**
     * Show what is in Hazelcast. Would be cleaner with lambdas.
     *
     * Don't assume keys are {@link Comparable} though they will be here.
     * Hence Map contents aren't sorted.
     */
    @CliCommand(value = "debugHZ", help = "List the Hazelcast content")
    public void debugHZ() {
        if (this.hazelcastInstance == null) {
            log.info("Hazelcast instance is null");
            return;
        }

        Collection<DistributedObject> distributedObjects = this.hazelcastInstance.getDistributedObjects();

        int mapCount = 0;

        for (DistributedObject distributedObject : distributedObjects) {

            if (distributedObject instanceof IMap) {
                mapCount++;

                try {
                    IMap<?, ?> iMap = (IMap<?, ?>) distributedObject;

                    log.info("IMap '{}'", iMap.getName());

                    int count = 0;
                    for (Map.Entry<?, ?> entry : iMap.entrySet()) {
                        count++;
                        log.info("     -> {}=='{}'", entry.getKey(), entry.getValue().toString().toLowerCase());
                    }
                    log.info("[{} row{}]", count, (count == 1 ? "" : "s"));
                } catch (Exception e) {
                    log.error(distributedObject.getName(), e);
                }
            }
        }

        if (mapCount == 0) {
            // Maps created lazily
            log.info("Hazelcast is empty");
        }

        log.info("");
    }
}
