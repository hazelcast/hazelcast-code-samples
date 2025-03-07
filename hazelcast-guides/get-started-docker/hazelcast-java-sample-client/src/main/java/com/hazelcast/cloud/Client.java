/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.cloud;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cloud.jobs.UpperCaseFunction;
import com.hazelcast.cloud.model.City;
import com.hazelcast.cloud.model.CitySerializer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import com.hazelcast.sql.SqlService;

/**
 * This is boilerplate application that configures client to connect Hazelcast Viridian cluster.
 * <p>
 * See: <a href="https://docs.hazelcast.com/cloud/get-started">https://docs.hazelcast.com/cloud/get-started</a>
 */
public class Client {
    public static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    public static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";

    private static String []hzServers;
    private static String hzClusterName;

    // guarantees to return a result or call System.exit
    private static String getRequiredProp(String propName) {
        String prop = System.getenv(propName);
        if (prop == null) {
            System.err.println("The " + propName + " property must be set");
            System.exit(1);
        }
        return prop;
    }

    private static void configure() {
        String hzServersProp = getRequiredProp(HZ_SERVERS_PROP);
        hzServers = hzServersProp.split(",");
        for (int i = 0; i < hzServers.length; ++i) hzServers[i] = hzServers[i].trim();

        hzClusterName = getRequiredProp(HZ_CLUSTER_NAME_PROP);
    }

    public static void main(String[] args) {
        configure();
        ClientConfig config = new ClientConfig();
        config.setClusterName(hzClusterName);
        for (String server : hzServers) config.getNetworkConfig().addAddress(server);

        // Register serializer of the City.
        config.getSerializationConfig().getCompactSerializationConfig().addSerializer(new CitySerializer());

        System.out.println("Connecting to Hazelcast");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        System.out.println("Connection Successful!");

        try {
            createMapping(client.getSql());
            insertCities(client);
            fetchCities(client.getSql());
            jetJobExample(client);
        } finally {
            client.shutdown();
        }
    }

    private static void createMapping(SqlService sqlService) {
        // See: https://docs.hazelcast.com/hazelcast/latest/sql/mapping-to-maps#compact-objects
        System.out.print("\nCreating mapping for cities...");

        String mappingSql = ""
                + "CREATE OR REPLACE MAPPING cities("
                + "     __key INT,"
                + "     country VARCHAR,"
                + "     city VARCHAR,"
                + "     population INT"
                + ") TYPE IMap"
                + " OPTIONS ("
                + "     'keyFormat' = 'int',"
                + "     'valueFormat' = 'compact',"
                + "     'valueCompactTypeName' = 'city'"
                + " )";

        try (SqlResult ignored = sqlService.execute(mappingSql)) {
            System.out.print("OK.");
        } catch (Exception ex) {
            System.out.print("FAILED. " + ex.getMessage());
        }
    }

    private static void insertCities(HazelcastInstance client) {
        try {
            System.out.print("\nCleaning up the 'cities' map...");
            client.getSql().execute("DELETE FROM cities");
            System.out.print("Cleanup...OK.");
            System.out.print("\nInserting cities into 'cities' map...");

            String insertQuery = "INSERT INTO cities "
                + "(__key, city, country, population) VALUES"
                + "(1, 'London', 'United Kingdom', 9540576),"
                + "(2, 'Manchester', 'United Kingdom', 2770434),"
                + "(3, 'New York', 'United States', 19223191),"
                + "(4, 'Los Angeles', 'United States', 3985520),"
                + "(5, 'Istanbul', 'Türkiye', 15636243),"
                + "(6, 'Ankara', 'Türkiye', 5309690),"
                + "(7, 'Sao Paulo ', 'Brazil', 22429800)";

            SqlResult result = client.getSql().execute(insertQuery);
            System.out.print("Insert...OK.");
        } catch (Exception ex) {
            System.out.print("FAILED. " + ex.getMessage());
        }

        // Let's also add a city as object.
        IMap<Integer, City> map = client.getMap("cities");

        City c = new City("Brazil", "Rio de Janeiro", 13634274);
        System.out.print("\nPutting a city into 'cities' map...");
        map.put(8, c);
        System.out.print("OK.");
    }

    private static void fetchCities(SqlService sqlService) {

        System.out.print("\nFetching cities via SQL...");

        try (SqlResult result = sqlService.execute("SELECT __key, this FROM cities")) {
            System.out.print("OK.\n");
            System.out.println("--Results of 'SELECT __key, this FROM cities'");

            System.out.printf("%4s | %20s | %20s | %15s |%n", "id", "country", "city", "population");
            for (SqlRow row : result) {
                int id = row.getObject("__key");
                City c = row.getObject("this");
                System.out.printf("%4s | %20s | %20s | %15s |%n",
                        id,
                        c.getCountry(),
                        c.getCity(),
                        c.getPopulation()
                );
            }
        } catch (Exception ex) {
            System.out.print("FAILED. " + ex.getMessage());
        }

        System.out.println("\n" +
            "!! Hint !! You can execute your SQL queries on your Viridian cluster using the \"SQL Broswer\" UI.\n" +
            "1. Start one of the preloaded demos in your Trial Experience.\n" +
            "2. This will open the 'SQL Browser'.\n" +
            "3. Add a new Tab.\n" +
            "4. Try to execute 'SELECT * FROM cities'.\n"
        );
    }

    /**
     * This example shows how to submit simple Jet job which uses logger as a sink. You will be able to see the results
     * of job execution in the Hazelcast cluster logs.
     *
     * @param client- a {@link HazelcastInstance} client.
     */
    private static void jetJobExample(HazelcastInstance client) {
        // See: https://docs.hazelcast.com/hazelcast/5.2/pipelines/submitting-jobs
        System.out.println("Submitting Jet job");

        BatchSource<String> items = TestSources.items(
                "United States", "Türkiye", "United Kingdom", "Poland", "Ukraine"
        );

        Pipeline pipeline = Pipeline.create()
                .readFrom(items)
                .map(new UpperCaseFunction())
                .writeTo(Sinks.logger()) // Results will be visible on the server logs.
                .getPipeline();

        JobConfig jobConfig = new JobConfig()
                .addClass(UpperCaseFunction.class);

        client.getJet().newJob(pipeline, jobConfig);

        System.out.println("Jet job submitted. \nYou can see the results in the logs (go to your cluster page in the Viridian console and click the 'Logs' link) or in Management Center - Jobs section (also available through the Viridian cluster page).");
    }
}
