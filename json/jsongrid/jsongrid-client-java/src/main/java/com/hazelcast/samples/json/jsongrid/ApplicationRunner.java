package com.hazelcast.samples.json.jsongrid;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.*;
import com.hazelcast.query.*;
import com.hazelcast.aggregation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * <p>Run some queries against the data.
 * </p>
 * <p>Although this is a Java client, the main logic is
 * querying and is based around JSON not Java, so easy to
 * port to other Hazelcast client languages.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationRunner implements CommandLineRunner {

    private static final String POTUS_IMAP_NAME = "POTUS";
    private static final String VPOTUS_IMAP_NAME = "VPOTUS";

    @Autowired
    private HazelcastInstance hazelcastInstance;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		this.confirmDataLoaded();
		System.out.println("- - - - - - - - - - - - - -");

		if (!this.testIfIMapExists(POTUS_IMAP_NAME)) {
			log.error("Cannot find IMap '{}', check server logs", POTUS_IMAP_NAME);
		}

		if (!this.testIfIMapExists(VPOTUS_IMAP_NAME)) {
			log.error("Cannot find IMap '{}', check server logs", VPOTUS_IMAP_NAME);
		}

		this.marchFourth();
		System.out.println("- - - - - - - - - - - - - -");

		this.johnForPresident();
		System.out.println("- - - - - - - - - - - - - -");

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("AJ: Running predicate query");


		IMap<?, ?> iMap = this.hazelcastInstance.getMap(POTUS_IMAP_NAME);

		//PredicateBuilder builder = Predicates.newPredicateBuilder();
		Predicate filter = Predicates.like( "FIRSTNAME", "John" );

		/*

		System.out.println(
			iMap.aggregate(Aggregators.longSum("ID"), Predicates.like("FIRSTNAME", "John"))
				);
				*/


		/*
		// Collection<?> results = iMap.values(Predicates.sql(sql));
		Collection<?> results = iMap.values(filter);

		for (Object result : results) {
			System.out.println(result);
		}
		*/

	}

    /**
     * <p>Confirm data actually exists to query. We could sort by name
     * if really necessary.
     * </p>
     * <p>The "{@code hazelcastInstance.getDistributedObjects()}" call
     * examines what is currently present, it does not force lazy evaluation of
     * what could be in the grid.
     * </p>
     */
    private void confirmDataLoaded() {
        Collection<DistributedObject> distributedObjects
            = this.hazelcastInstance.getDistributedObjects();

        distributedObjects
        .stream()
        .filter(distributedObject -> distributedObject instanceof IMap)
        .forEach(distributedObject -> {
            IMap<?, ?> iMap = (IMap<?, ?>) distributedObject;

            // Don't use size() in production, not optimal. use isEmpty()
            System.out.printf("IMap '%s', size %d%n",
                    iMap.getName(), iMap.size()
                    );
        });

        if (distributedObjects.size() != 2) {
            log.warn("Expected just 2 distributed objects, found {}",
                    distributedObjects.size());
        }
    }

    /**
     * <p>Slightly unnecessary (given {@link #confirmDataLoaded})
     * test for a named {@link com.hazelcast.core.IMap IMap}.
     * </p>
     *
     * @param iMapName
     * @return Yes if there, but doesn't force created
     */
    private boolean testIfIMapExists(String iMapName) {

        Collection<DistributedObject> results =
                this.hazelcastInstance.getDistributedObjects()
                .stream()
                .filter(distributedObject -> distributedObject.getName().equals(iMapName))
                .filter(distributedObject -> distributedObject instanceof IMap)
                .collect(Collectors.toSet());

        return results.size() == 1;
    }

    /**
     * <p>Historically the fourth of March was the inauguration date, although
     * more recently moved to the twenth of January. Find which vice-presidents
     * took office that day.
     * <p>
     */
    private void marchFourth() {
        String sql = ""
                + "TOOKOFFICE LIKE '%-03-04'"
                + "";

        this.runQuery(sql, VPOTUS_IMAP_NAME);
    }

    /**
     * <p>"<b>John</b>" is a common name amongst American presidents.
     * But which ones are they ?
     * </p>
     */
    private void johnForPresident() {
        String sql = ""
                + "FIRSTNAME = 'John'"
                + " OR "
                + "MIDDLENAME1 = 'John'"
                + " OR "
                + "MIDDLENAME2 = 'John'"
                + " OR "
                + "LASTNAME = 'John'"
                + "";

        this.runQuery(sql, POTUS_IMAP_NAME);
    }

    /**
     * <p>Helper method to run a generic query against a
     * any map and output the results to the console as
     * JSON.
     * </p>
     *
     * @param sql Query as a Strng, not exactly SQL but looks close
     * @param iMapName Map to execute against, assume not null
     */
    private void runQuery(String sql, String iMapName) {
        System.out.println(sql);

        IMap<?, ?> iMap = this.hazelcastInstance.getMap(iMapName);

        Collection<?> results = iMap.values(Predicates.sql(sql));

        for (Object result : results) {
            System.out.println(result);
        }

        if (results.size() == 0) {
            System.out.println("*** No results ***");
        } else {
            System.out.printf("[ %d result%s]%n", results.size(), (results.size() == 1 ? "" : "s"));
        }
    }
}
