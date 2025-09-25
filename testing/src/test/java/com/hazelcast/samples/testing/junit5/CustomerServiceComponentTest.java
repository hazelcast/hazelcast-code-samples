package com.hazelcast.samples.testing.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapStore;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.CustomerService;
import com.hazelcast.samples.testing.HzCustomerService;
import com.hazelcast.samples.testing.SQLCustomerMapStore;
import com.hazelcast.samples.testing.ServiceException;
import com.hazelcast.sql.HazelcastSqlException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.hazelcast.test.HazelcastTestSupport.randomName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Component test for {@link HzCustomerService} with a real
 * {@link com.hazelcast.map.MapStore} integration.
 *
 * <p>Demonstrates wiring a real MapStore (H2) to an IMap, exercising eviction → reload and surfacing downstream failures cleanly.
 */
public class CustomerServiceComponentTest {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS customers (id VARCHAR PRIMARY KEY, name VARCHAR)";
    private static final String DROP_TABLE_SQL =
            "DROP TABLE IF EXISTS customers";

    private TestHazelcastFactory factory;
    private Connection conn;

    @BeforeEach
    void setup() throws SQLException {
        factory = new TestHazelcastFactory();
        conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        conn.createStatement().execute(CREATE_TABLE_SQL);
    }

    @AfterEach
    void teardown() throws SQLException {
        if (conn != null) {
            conn.createStatement().execute(DROP_TABLE_SQL);
            conn.close();
        }
        if (factory != null) {
            factory.shutdownAll();
        }
    }

    /**
     * Verify that customers are saved to both the map and the database,
     * and can be reloaded from {@link SQLCustomerMapStore} after eviction.
     */
    @Test
    void customerServiceWithMapStoreInteractions() {
        Config config = new Config();
        config.setClusterName(randomName());
        config.getMapConfig("customers")
              .getMapStoreConfig()
              .setEnabled(true)
              .setImplementation(new SQLCustomerMapStore(conn));

        HazelcastInstance hz = factory.newHazelcastInstance(config);
        CustomerService service = new HzCustomerService(hz);

        service.save(new Customer("c1", "Alice"));
        Customer fromMap = service.findCustomer("c1");

        hz.getMap("customers").evictAll();
        Customer fromStore = service.findCustomer("c1");

        assertEquals("Alice", fromMap.name());
        assertEquals("Alice", fromStore.name());
    }

    /**
     * Verify that a MapStore failure is wrapped in a {@link ServiceException}.
     */
    @Test
    void customerServiceWithMapStoreFailure() {
        @SuppressWarnings("unchecked")
        MapStore<String, Customer> failingMapStore = (MapStore<String, Customer>) mock(MapStore.class);
        when(failingMapStore.load("c1"))
                .thenThrow(new HazelcastSqlException("Injected failure",
                        new SQLException("downstream DB error")));

        Config config = new Config();
        config.setClusterName(randomName());
        config.getMapConfig("customers")
              .getMapStoreConfig()
              .setEnabled(true)
              .setImplementation(failingMapStore);

        HazelcastInstance hz = factory.newHazelcastInstance(config);
        CustomerService service = new HzCustomerService(hz);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.findCustomer("c1"));

        assertEquals("Find customer failed", ex.getMessage());
        assertEquals("Injected failure", ex.getCause().getMessage());
    }
}
