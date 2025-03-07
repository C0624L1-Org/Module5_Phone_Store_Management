package com.example.last.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeRepository {

    private static final String QUERY_FOR_DELETE_EMPLOYEE = "DELETE FROM employee WHERE employeeID = ?";
    private final JdbcTemplate jdbcTemplate;

    // Constructor injection
    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteById(Long id) {
        int rowsAffected = jdbcTemplate.update(QUERY_FOR_DELETE_EMPLOYEE, id);

        if (rowsAffected > 0) {
            System.out.println("Deleted employee with ID " + id);
        } else {
            System.out.println("No employee found with ID " + id);
        }
    }
}
