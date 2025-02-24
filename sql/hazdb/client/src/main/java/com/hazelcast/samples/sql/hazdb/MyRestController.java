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
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;

/**
 * <p>
 * For use by React or directly.
 */
@RestController
@RequestMapping("/rest")
public class MyRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyRestController.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * <p>
     * Use the {@link JdbcTemplate} to execute an SQL query.
     * </p>
     */
    @GetMapping(value = "/jdbcNoParam", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jdbcNoParam(@RequestParam("sql") String sql) {
        LOGGER.debug("jdbcNoParam('{}')", sql);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");

        try {
            List<?> list = this.jdbcTemplate.queryForList(sql);
            Iterator<?> iterator = list.iterator();

            MyRestController.formatResult(iterator, stringBuilder);

        } catch (Exception e) {
            stringBuilder.append("\"error\":\"").append(Utils.makeText(e.getMessage())).append("\"");
            stringBuilder.append(",\"rows\":[]");
            stringBuilder.append(",\"warning\":\"\"");
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    /**
     * <p>
     * Use the {@link JdbcTemplate} to execute an SQL query with one {@code String}
     * parameter.
     * </p>
     */
    @GetMapping(value = "/jdbc1StringParam", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jdbc1StringParam(@RequestParam("sql") String sql, @RequestParam("param") String param) {
        LOGGER.debug("jdbc1StringParam('{}', '{}')", sql, param);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");

        try {
            List<?> list = this.jdbcTemplate.queryForList(sql, param);
            Iterator<?> iterator = list.iterator();

            MyRestController.formatResult(iterator, stringBuilder);

        } catch (Exception e) {
            stringBuilder.append("\"error\":\"").append(Utils.makeText(e.getMessage())).append("\"");
            stringBuilder.append(",\"rows\":[]");
            stringBuilder.append(",\"warning\":\"\"");
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    /**
     * <p>
     * Use the {@link JdbcTemplate} to execute an SQL query with one {@code Integer}
     * parameter.
     * </p>
     */
    @GetMapping(value = "/jdbc1IntegerParam", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jdbc1IntegerParam(@RequestParam("sql") String sql, @RequestParam("param") int param) {
        LOGGER.debug("jdbc1IntegerParam('{}', '{}')", sql, param);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");

        try {
            List<?> list = this.jdbcTemplate.queryForList(sql, param);
            Iterator<?> iterator = list.iterator();

            MyRestController.formatResult(iterator, stringBuilder);

        } catch (Exception e) {
            stringBuilder.append("\"error\":\"").append(Utils.makeText(e.getMessage())).append("\"");
            stringBuilder.append(",\"rows\":[]");
            stringBuilder.append(",\"warning\":\"\"");
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    /**
     * <p>
     * Directly passes SQL query to Hazelcast, and wraps the result as JSON for web
     * display.
     * </p>
     */
    @GetMapping(value = "/sqlQuery", produces = MediaType.APPLICATION_JSON_VALUE)
    public String sqlQuery(@RequestParam("sql") String sql) {
        LOGGER.debug("sqlQuery('{}')", sql);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");

        try {
            SqlResult sqlResult = this.hazelcastInstance.getSql().execute(sql);
            Iterator<SqlRow> iterator = sqlResult.iterator();

            MyRestController.formatResult(iterator, stringBuilder);

        } catch (Exception e) {
            stringBuilder.append("\"error\":\"").append(Utils.makeText(e.getMessage())).append("\"");
            stringBuilder.append(",\"rows\":[]");
            stringBuilder.append(",\"warning\":\"\"");
        }

        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    /**
     * <p>
     * Directly passes SQL query to Hazelcast, and wraps the result as JSON for web
     * display. An update/delete/insert returns no rows.
     * </p>
     * <p>
     * If you wanted to use {@code JdbcTemplate} for this, the code would be
     * similar:
     * </p>
     *
     * <pre>
     * String sql = "DELETE FROM bundesliga WHERE __key = ?";
     * Object[] args = new String[] { "Bayern Munich" };
     * this.jdbcTemplate.update(sql, args);
     * </pre>
     */
    @GetMapping(value = "/sqlUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
    public String sqlUpdate(@RequestParam("sql") String sql) {
        LOGGER.debug("sqlUpdate('{}')", sql);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");

        try {
            this.hazelcastInstance.getSql().execute(sql);
            stringBuilder.append("\"error\":\"").append("\"");
        } catch (Exception e) {
            stringBuilder.append("\"error\":\"").append(Utils.makeText(e.getMessage())).append("\"");
        }
        stringBuilder.append(",\"rows\":[]");
        stringBuilder.append(",\"warning\":\"\"");

        stringBuilder.append("}");

        return stringBuilder.toString();
    }

    /**
     * <p>
     * Common logic to this class to format SQL result.
     * </p>
     *
     * @param iterator      Iterator on something, don't need the class as using
     *                      "{@code toString()}"
     */
    private static void formatResult(Iterator<?> iterator, StringBuilder stringBuilder) {
        // Worked, even if no rows found
        stringBuilder.append("\"error\":\"").append("\"");

        int count = 0;
        stringBuilder.append(",\"rows\":[");
        while (count < MyLocalConstants.SQL_LIMIT && iterator.hasNext()) {
            if (count > 0) {
                stringBuilder.append(",");
            }
            count++;
            Object row = iterator.next();
            stringBuilder.append("\"").append(Utils.makeText(Objects.toString(row))).append("\"");
        }
        stringBuilder.append("]");

        // Truncated?
        if (iterator.hasNext()) {
            stringBuilder.append(",\"warning\":\"Truncated at ").append(MyLocalConstants.SQL_LIMIT).append(" rows\"");
        } else {
            stringBuilder.append(",\"warning\":\"\"");
        }
    }

    /**
     * <p>
     * For Kubernetes readiness/liveness probe
     * </p>
     */
    @GetMapping("/k8s")
    public String k8s() {
        LOGGER.debug("k8s()");

        Boolean running = this.hazelcastInstance.getLifecycleService().isRunning();

        if (running) {
            return running.toString();
        } else {
            throw new RuntimeException("Running==" + running);
        }
    }

}
