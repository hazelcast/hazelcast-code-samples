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
 * Why This is a Component Test:
 * <ul>
 *  <li>Involves the component under test (HzCustomerService) and its private dependencies (SQLCustomerMapStore via Hazelcast)</li>
 *  <li>Uses a real external dependency (H2) and real Hazelcast instance (not mocked)</li>
 *  <li>Tests distributed behaviour: eviction, load and reload, failure of downstream dependencies</li>
 * </ul>
 */
public class CustomerServiceComponentTest {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS customers (id VARCHAR PRIMARY KEY, name VARCHAR)";
    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS customers";
    private TestHazelcastFactory factory;
    private Connection conn;

    @BeforeEach
    void setup()
            throws SQLException {
        factory = new TestHazelcastFactory();
        conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        conn.createStatement().execute(CREATE_TABLE_SQL);
    }

    @AfterEach
    void teardown()
            throws SQLException {
        if (conn != null) {
            conn.createStatement().execute(DROP_TABLE_SQL);
            conn.close();
        }
        if (factory != null) {
            factory.shutdownAll();
        }
    }

    @Test
    void customerServiceWithMapStoreInteractions() {

        // Configure Hazelcast with MapStore implementation using the H2 connection
        Config config = new Config();
        config.setClusterName(randomName());
        // inject custom Map Store
        config.getMapConfig("customers").getMapStoreConfig().setEnabled(true).setImplementation(new SQLCustomerMapStore(conn));

        // Start a real embedded Hazelcast instance
        HazelcastInstance hz = factory.newHazelcastInstance(config);

        // Use real CustomerService backed by Hazelcast
        CustomerService service = new HzCustomerService(hz);

        // Save customer (should persist to both IMap and DB)
        service.save(new Customer("c1", "Alice")); // should go into both IMap and DB
        Customer fromMap = service.findCustomer("c1");      // should be from IMap

        // Evict IMap to force reload from MapStore (H2)
        hz.getMap("customers").evictAll();
        Customer fromStore = service.findCustomer("c1");    // should be reloaded from H2

        // Verify both fetches return the same persisted data
        assertEquals("Alice", fromMap.name());
        assertEquals("Alice", fromStore.name());
    }

    @Test
    void customerServiceWithMapStoreFailure() {

        // Create a mock MapStore that throws an exception when load is called
        MapStore<String, Customer> failingMapStore = (MapStore<String, Customer>) mock(MapStore.class);
        when(failingMapStore.load("c1")).thenThrow(new HazelcastSqlException("Injected failure", new SQLException("downstream DB error")));

        // Configure Hazelcast with the failing MapStore
        Config config = new Config();
        config.setClusterName(randomName());
        config.getMapConfig("customers").getMapStoreConfig().setEnabled(true).setImplementation(failingMapStore);

        HazelcastInstance hz = factory.newHazelcastInstance(config);
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
