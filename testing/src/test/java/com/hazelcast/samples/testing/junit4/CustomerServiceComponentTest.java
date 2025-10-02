package com.hazelcast.samples.testing.junit4;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapStore;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.CustomerService;
import com.hazelcast.samples.testing.HzCustomerService;
import com.hazelcast.samples.testing.SQLCustomerMapStore;
import com.hazelcast.samples.testing.ServiceException;
import com.hazelcast.sql.HazelcastSqlException;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Component test for {@link HzCustomerService} with a real
 * {@link com.hazelcast.map.MapStore} integration.
 *
 * <p>Demonstrates wiring a real MapStore (H2) to an IMap, exercising eviction → reload and surfacing downstream failures cleanly.
 */
@RunWith(JUnit4.class)
public class CustomerServiceComponentTest extends HazelcastTestSupport {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE customers (id VARCHAR PRIMARY KEY, name VARCHAR)";
    private static final String DROP_TABLE_SQL = "DROP TABLE customers";

    private HazelcastInstance hz;
    private Connection conn;

    @Before
    public void setUp() throws Exception {
        // Initialise in-memory H2 database
        conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        conn.createStatement().execute(CREATE_TABLE_SQL);
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null) {
            conn.createStatement().execute(DROP_TABLE_SQL);
        }
        if (hz != null) {
            hz.shutdown();
        }
    }

    /**
     * Verify that customers are written through to the database and
     * reloaded via MapStore after eviction from the IMap.
     */
    @Test
    public void customerServiceWithMapStoreInteractions() {
        Config config = new Config();
        config.setClusterName(randomName());
        config.getMapConfig("customers")
              .getMapStoreConfig()
              .setEnabled(true)
              .setImplementation(new SQLCustomerMapStore(conn));

        assertNotNull(conn);
        hz = createHazelcastInstance(config);

        CustomerService service = new HzCustomerService(hz);

        service.save(new Customer("c1", "Alice"));
        Customer fromMap = service.findCustomer("c1");

        hz.getMap("customers").evictAll();
        Customer fromStore = service.findCustomer("c1");

        assertEquals("Alice", fromMap.name());
        assertEquals("Alice", fromStore.name());
    }

    /**
     * Verify that downstream MapStore failures are wrapped
     * in a {@link ServiceException}.
     */
    @Test
    public void customerServiceWithMapStoreFailure() {
        @SuppressWarnings("unchecked")
        MapStore<String, Customer> failingMapStore = (MapStore<String, Customer>) mock(MapStore.class);
        when(failingMapStore.load("c1"))
                .thenThrow(new HazelcastSqlException("Injected failure", new SQLException("downstream DB error")));

        Config config = new Config();
        config.setClusterName(randomName());
        config.getMapConfig("customers")
              .getMapStoreConfig()
              .setEnabled(true)
              .setImplementation(failingMapStore);

        hz = createHazelcastInstance(config);
        CustomerService service = new HzCustomerService(hz);

        // Assert that the service wraps the exception in a ServiceException
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            // Action that triggers failure
            service.findCustomer("c1");
        });

        assertEquals("Find customer failed", ex.getMessage());
        assertEquals("Injected failure", ex.getCause().getMessage());
    }
}
