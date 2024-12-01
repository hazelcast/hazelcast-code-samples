package com.hazelcast.samples.map;

import com.hazelcast.map.MapLoader;
import com.hazelcast.map.MapStore;
import com.hazelcast.samples.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hazelcast.samples.connection.pool.HikariDataSourcePool.getConnection;

public class EmployeeMapStore implements MapLoader<Integer, Employee>, MapStore<Integer, Employee> {

    public EmployeeMapStore() {
    }

    @Override
    public Iterable<Integer> loadAllKeys() {
        String query = "SELECT EMPID FROM EMPLOYEE";

        List<Integer> empIds = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                empIds.add(resultSet.getInt(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error on load all keys : " + exception);
        }

        return empIds;
    }

    @Override
    public Employee load(Integer empId) {
        String query = "SELECT EMPID, NAME, SALARY FROM EMPLOYEE WHERE EMPID=?";
        Employee.EmployeeBuilder employeeBuilder = Employee.builder();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, empId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                employeeBuilder
                        .empId(resultSet.getInt(1))
                        .name(resultSet.getString(2))
                        .salary(resultSet.getDouble(3));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error on load key : " + exception);
        }

        return employeeBuilder.build();
    }

    @Override
    public Map<Integer, String> loadAll(Collection collection) {
        System.out.println("Load all employee..");

        List<Integer> employees = (List<Integer>) collection;

        return employees.stream().collect(Collectors.toMap(id -> id, id -> load(id).toString()));
    }

    @Override
    public void store(Integer integer, Employee employee) {
        String storeQuery = "INSERT INTO EMPLOYEE(EMPID, NAME, SALARY) VALUES(?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(storeQuery)) {
            preparedStatement.setInt(1, employee.getEmpId());
            preparedStatement.setString(2, employee.getName());
            preparedStatement.setDouble(3, employee.getSalary());

            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            System.out.println("Exception : " + exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }


    @Override
    public void storeAll(Map<Integer, Employee> map) {
        //map.forEach(this::store);
        //or
        String storeQuery = "INSERT INTO EMPLOYEE(EMPID, NAME, SALARY) VALUES(?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(storeQuery)) {
            map.forEach((identity, employee) -> {

                try {
                    preparedStatement.setInt(1, employee.getEmpId());
                    preparedStatement.setString(2, employee.getName());
                    preparedStatement.setDouble(3, employee.getSalary());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            int[] batchResults = preparedStatement.executeBatch();

            // Handle the results if needed
            for (int result : batchResults) {
                //TODO: add this if needed
            }
        } catch (SQLException exception) {
            System.out.println("Exception : " + exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public void delete(Integer integer) {
    }

    @Override
    public void deleteAll(Collection<Integer> collection) {
    }
}
