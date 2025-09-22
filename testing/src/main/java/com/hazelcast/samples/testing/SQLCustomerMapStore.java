package com.hazelcast.samples.testing;

import com.hazelcast.map.MapStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public class SQLCustomerMapStore
        implements MapStore<String, Customer> {
    private final Connection connection;

    public SQLCustomerMapStore(Connection connection) {
        this.connection = connection;
    }

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

    @Override
    public Map<String, Customer> loadAll(Collection<String> collection) {
        return Map.of();
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return null;
    }

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

    @Override
    public void storeAll(Map<String, Customer> map) {

    }

    @Override
    public void delete(String s) {

    }

    @Override
    public void deleteAll(Collection<String> collection) {

    }
}
