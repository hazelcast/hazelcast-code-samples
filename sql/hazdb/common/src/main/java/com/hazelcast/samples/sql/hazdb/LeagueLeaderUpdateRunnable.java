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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.sql.SqlRow;

/**
 * <p>
 * Check if the league leader needs updating.
 * The same could be achieved with SQL, select the first from "{@code bundesliga}" map
 * ordered by points.
 * This is similar to a materialized view, updated by a trigger.
 * </p>
 * <p>
 * TODO: Does not currently handle a tie for the lead.
 * </p>
 */
public class LeagueLeaderUpdateRunnable implements HazelcastInstanceAware, Runnable, Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeagueLeaderUpdateRunnable.class);
    private static final long serialVersionUID = 1L;

    private HazelcastInstance hazelcastInstance;

    @Override
    public void run() {
        LOGGER.info("run()");

        Integer key = Integer.valueOf(0);

        String sql1 = "SELECT a.*, b.__key, MAX(b.points)"
                + "FROM TABLE(generate_series(?, ?)) AS a, " + MyConstants.IMAP_NAME_BUNDESLIGA + " AS b"
                + " GROUP BY a.v, b.__key ORDER BY 3 DESC LIMIT 1";
        Object[] params1 = new Object[] { key, key };

        SqlRow sqlRow;
        try {
            sqlRow = this.hazelcastInstance.getSql().execute(sql1, params1).iterator().next();

            LOGGER.trace("SQL1: Key {}, current leader {} & {}",
                    sqlRow.getObject(0).toString(),
                    sqlRow.getObject(1).toString(),
                    sqlRow.getObject(2).toString());
        } catch (Exception e) {
            LOGGER.error("run():" + sql1, e);
            return;
        }

        this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_LEADER).entrySet()
        .forEach(entry -> {
            LOGGER.trace("'{}', before SQL2: '{}'", MyConstants.IMAP_NAME_LEADER, entry);
        });

        // "INSERT" will not overwrite, needs to "SINK"
        String sql2 = "SINK INTO " + MyConstants.IMAP_NAME_LEADER
                + " VALUES (?, ?, ?)";
        Object[] params2 = new Object[3];
        params2[0] = sqlRow.getObject(0);
        params2[1] = sqlRow.getObject(1);
        params2[2] = sqlRow.getObject(2);

        try {
            this.hazelcastInstance.getSql().execute(sql2, params2);
        } catch (Exception e) {
            LOGGER.error("run():" + sql2, e);
        }

        this.hazelcastInstance.getMap(MyConstants.IMAP_NAME_LEADER).entrySet()
        .forEach(entry -> {
            LOGGER.trace("'{}', after SQL2: '{}'", MyConstants.IMAP_NAME_LEADER, entry);
        });
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance arg0) {
        this.hazelcastInstance = arg0;
    }

}
