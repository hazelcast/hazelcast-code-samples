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

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;

/**
 * <p>
 * Initialize once, although idempotent.
 * </p>
 */
@Configuration
public class ApplicationInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitializer.class);

    @Autowired
    private BundesligaListenerTrigger bundesligaListenerTrigger;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            int clusterSize = this.hazelcastInstance.getCluster().getMembers().size();

            // First node to start does initialization
            if (clusterSize == 1) {
                LOGGER.info("-=-=-=-=- '{}' cluster size {}, initialization start -=-=-=-=-=-",
                        this.hazelcastInstance.getName(), clusterSize);

                boolean ok = true;
                ok = this.createMappings();
                ok = ok & this.createMaps();
                ok = ok & this.createViews();
                ok = ok & this.createTestData();
                ok = ok & this.queryTestData();

                if (ok) {
                    LOGGER.info("-=-=-=-=- '{}' cluster size {}, initialization end   -=-=-=-=-=-",
                            this.hazelcastInstance.getName(), clusterSize);
                } else {
                    LOGGER.error("-=-=-=-=- '{}' cluster size {}, initialization failed -=-=-=-=-=-",
                            this.hazelcastInstance.getName(), clusterSize);
                    this.hazelcastInstance.shutdown();
                }
            } else {
                LOGGER.info("-=-=-=-=- '{}' cluster size {}, assume initialized -=-=-=-=-=-",
                        this.hazelcastInstance.getName(), clusterSize);
            }

            // Each node attaches a Spring @Bean triggering actions in response to data
            // changes on that node
            this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_BUNDESLIGA)
                    .addLocalEntryListener(this.bundesligaListenerTrigger);

        };
    }

    /**
     * <p>
     * Mappings define the data structure for querying.
     * </p>
     *
     * @return
     */
    public boolean createMappings() {
        String bundesligaBody = "("
                + "    \"__key\" VARCHAR,"
                + "    \"played\" INTEGER,"
                + "    \"won\" INTEGER,"
                + "    \"drawn\" INTEGER,"
                + "    \"lost\" INTEGER,"
                + "    \"goals_for\" INTEGER,"
                + "    \"goals_against\" INTEGER,"
                + "    \"goal_difference\" INTEGER,"
                + "    \"points\" INTEGER"
                + ")"
                + " TYPE IMap "
                + " OPTIONS ( "
                + " 'keyFormat' = 'java',"
                + " 'keyJavaClass' = '" + String.class.getCanonicalName() + "',"
                + " 'valueFormat' = 'json-flat'"
                + " )";

        String heartbeatBody = "("
                + "    \"node\" VARCHAR EXTERNAL NAME \"__key.node\","
                + "    \"timestamp\" BIGINT EXTERNAL NAME \"__key.timestamp\","
                + "    \"freeMemory\" BIGINT,"
                + "    \"usedMemory\" BIGINT"
                + ")"
                + " TYPE IMap "
                + " OPTIONS ( "
                + " 'keyFormat' = 'json-flat',"
                + " 'valueFormat' = 'json-flat'"
                + " )";

        // See also this.createViews()
        String leaderBody = "("
                + "    \"__key\" INTEGER,"
                + "    \"name\" VARCHAR,"
                + "    \"points\" INTEGER"
                + ")"
                + " TYPE IMap "
                + " OPTIONS ( "
                + " 'keyFormat' = 'java',"
                + " 'keyJavaClass' = '" + Integer.class.getCanonicalName() + "',"
                + " 'valueFormat' = 'json-flat'" + " )";

        String stadiumBody = "("
                + "    \"__key\" VARCHAR,"
                + "    \"this\" VARCHAR" + ")"
                + " TYPE IMap "
                + " OPTIONS ( " + " 'keyFormat' = 'java',"
                + " 'keyJavaClass' = '" + String.class.getCanonicalName() + "'," 
                + " 'valueFormat' = 'java',"
                + " 'valueJavaClass' = '" + String.class.getCanonicalName() + "'"
                + " )";

        boolean ok = this.defineMapping(MyConstants.IMAP_NAME_BUNDESLIGA, bundesligaBody);
        ok = ok & this.defineMapping(MyConstants.IMAP_NAME_HEARTBEAT, heartbeatBody);
        ok = ok & this.defineMapping(MyConstants.IMAP_NAME_LEADER, leaderBody);
        ok = ok & this.defineMapping(MyConstants.IMAP_NAME_STADIUM, stadiumBody);
        return ok;
    }

    /**
     * <p>
     * Apply a mapping, catch errors
     * </p>
     *
     * @param mapName
     * @param mappingBody
     * @return
     */
    private boolean defineMapping(String mapName, String mappingBody) {
        String mapping = "CREATE OR REPLACE MAPPING \"" + mapName + "\" " + mappingBody;
        try {
            LOGGER.debug("Definition '{}'", mapping);
            this.hazelcastInstance.getSql().execute(mapping);
            return true;
        } catch (Exception e) {
            LOGGER.error(mapping, e);
            return false;
        }
    }

    /**
     * <p>
     * Maps are created when first accessed, so do this now to force creation.
     * </p>
     *
     * @return Always {@code true}, no failure visible here
     */
    private boolean createMaps() {
        for (String mapName : MyConstants.IMAP_NAMES) {
            this.hazelcastInstance.getMap(mapName);
        }
        return true;
    }

    /**
     * <p>
     * Define views onto the mappings. View name is base map name with "{@code _v}"
     * suffix.
     * </p>
     *
     * @return
     */
    private boolean createViews() {
        String view1 = "CREATE OR REPLACE VIEW " + MyConstants.IMAP_NAME_LEADER + MyConstants.VIEW_SUFFIX
                + " AS SELECT "
                + "    __key AS \"position\""
                + "    ,name"
                + " FROM " + MyConstants.IMAP_NAME_LEADER;

        boolean ok = this.defineMapView(view1);
        return ok;
    }

    /**
     * <p>
     * Apply a view onto a single map, catch errors.
     * </p>
     *
     * @param view
     * @return
     */
    private boolean defineMapView(String view) {
        try {
            LOGGER.debug("Definition '{}'", view);
            this.hazelcastInstance.getSql().execute(view);
            return true;
        } catch (Exception e) {
            LOGGER.error(view, e);
            return false;
        }
    }

    /**
     * <p>
     * Insert teams and stadiums, derive the leader of the league.
     * </p>
     *
     * @return
     */
    private boolean createTestData() {
        IMap<String, HazelcastJsonValue> bundesligaMap = this.hazelcastInstance
                .getMap(MyConstants.IMAP_NAME_BUNDESLIGA);
        IMap<Integer, HazelcastJsonValue> leaderMap = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_LEADER);
        IMap<String, String> stadiumMap = this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_STADIUM);

        try {
            for (Object[] row : TestData.BUNDESLIGA) {
                String key = row[0].toString();
                String value = "{"
                        + "\"played\":" + row[1]
                        + ",\"won\":" + row[2]
                        + ",\"drawn\":" + row[3]
                        + ",\"lost\":" + row[4]
                        + ",\"goals_for\":" + row[5]
                        + ",\"goals_against\":" + row[6]
                        + ",\"goal_difference\":" + row[7]
                        + ",\"points\":" + row[8] + "}";

                bundesligaMap.set(key, new HazelcastJsonValue(value));
            }
        } catch (Exception e) {
            LOGGER.error("createTestData():" + bundesligaMap.getName(), e);
            return false;
        }
        LOGGER.info("Inserted {} into '{}'", TestData.BUNDESLIGA.length, bundesligaMap.getName());

        try {
            this.deriveLeader(leaderMap);
        } catch (Exception e) {
            LOGGER.error("createTestData():" + leaderMap.getName(), e);
            return false;
        }
        LOGGER.info("Inserted {} into '{}'", leaderMap.size(), leaderMap.getName());

        try {
            for (String[] row : TestData.STADIA) {
                stadiumMap.set(row[0], row[1]);
            }
        } catch (Exception e) {
            LOGGER.error("createTestData():" + stadiumMap.getName(), e);
            return false;
        }
        LOGGER.info("Inserted {} into '{}'", TestData.STADIA.length, stadiumMap.getName());

        return true;
    }

    /**
     * <p>
     * Who's winning? The team with the most points...
     * </p>
     * TODO: Make this a JSON Array to cope with a tie.
     */
    private void deriveLeader(IMap<Integer, HazelcastJsonValue> leaderMap) throws Exception {
        String sql = "SELECT __key, MAX(points) FROM " + MyConstants.IMAP_NAME_BUNDESLIGA
                + " GROUP BY __key ORDER BY 2 DESC LIMIT 1";

        // Assume one row
        SqlRow sqlRow = this.hazelcastInstance.getSql().execute(sql).iterator().next();
        // Assume one column
        HazelcastJsonValue value = new HazelcastJsonValue(
                "{" + "\"name\":\"" + sqlRow.getObject(0) + "\"" + ",\"points\":" + sqlRow.getObject(1) + "}");

        // Zero base
        leaderMap.put(Integer.valueOf(0), value);
    }

    /**
     * <p>
     * Run some SQL to confirm everything is working.
     * </p>
     *
     * @return
     */
    private boolean queryTestData() {
        String sql1 = "SELECT * FROM " + MyConstants.IMAP_NAME_BUNDESLIGA + " ORDER BY points DESC LIMIT 3";
        String sql2 = "SELECT * FROM " + MyConstants.IMAP_NAME_LEADER;
        String sql3 = "SELECT * FROM " + MyConstants.IMAP_NAME_LEADER + MyConstants.VIEW_SUFFIX;
        String sql4 = "SELECT * FROM " + MyConstants.IMAP_NAME_STADIUM;
        String sql5 = "SELECT a.__key AS \"stadium\", UPPER(b.__key) AS \"Team\", b.points "
                + "FROM "
                + MyConstants.IMAP_NAME_STADIUM + " AS a "
                + "LEFT JOIN " + MyConstants.IMAP_NAME_BUNDESLIGA + " AS b "
                + "ON a.this = b.__key";

        for (String sql : List.of(sql1, sql2, sql3, sql4, sql5)) {
            try {
                LOGGER.debug("=====================================================");
                LOGGER.debug("----");
                LOGGER.debug("Test query: '{}'", sql);
                LOGGER.debug("----");

                SqlResult sqlResult = this.hazelcastInstance.getSql().execute(sql);

                int count = 0;
                Iterator<SqlRow> iterator = sqlResult.iterator();

                while (iterator.hasNext()) {
                    SqlRow sqlRow = iterator.next();
                    LOGGER.debug(" {}", sqlRow);
                    count++;
                }

                LOGGER.debug("----");
                LOGGER.debug("Result set size = '{}'", count);
                LOGGER.debug("----");
            } catch (Exception e) {
                LOGGER.error("queryTestData():" + sql, e);
                return false;
            }
        }
        LOGGER.debug("=====================================================");

        return true;
    }
}

