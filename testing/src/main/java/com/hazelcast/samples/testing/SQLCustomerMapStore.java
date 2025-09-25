package com.hazelcast.samples.testing;

import com.hazelcast.map.MapStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * {@link MapStore} implementation that persists {@link Customer} data
 * to a SQL-compliant database.
 *
 * <p>Provides on-demand loading and storing of customers via JDBC.
 * This example keeps most bulk operations unimplemented.
 */
public class SQLCustomerMapStore implements MapStore<String, Customer> {
    private final Connection connection;

    /**
     * Create a new store bound to a JDBC connection.
     *
     * @param connection open JDBC connection to use for persistence
     */
    public SQLCustomerMapStore(Connection connection) {
        this.connection = connection;
    }

    /**
     * Load a customer by identifier.
     *
     * @param key customer identifier
     * @return matching customer, or {@code null} if not found
     */
    @Override
    public Customer load(String key) {
        try (var stmt = connection.prepareStatement("SELECT id, name FROM customers WHERE id = ?")) {
            stmt.setString(1, key);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(rs.getString("id"), rs.getString("name"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Bulk load is not implemented in this example.
     */
    @Override
    public Map<String, Customer> loadAll(Collection<String> collection) {
        return Map.of();
    }

    /**
     * Key enumeration is not implemented in this example.
     */
    @Override
    public Iterable<String> loadAllKeys() {
        return null;
    }

    /**
     * Store or update a customer using a SQL {@code MERGE}.
     *
     * @param key      customer identifier
     * @param customer customer data to persist
     */
    @Override
    public void store(String key, Customer customer) {
        try (var stmt = connection.prepareStatement("MERGE INTO customers KEY(id) VALUES (?, ?)")) {
            stmt.setString(1, customer.id());
            stmt.setString(2, customer.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Bulk store is not implemented in this example.
     */
    @Override
    public void storeAll(Map<String, Customer> map) {
    }

    /**
     * Delete is not implemented in this example.
     */
    @Override
    public void delete(String s) {
    }

    /**
     * Bulk delete is not implemented in this example.
     */
    @Override
    public void deleteAll(Collection<String> collection) {
    }
}
