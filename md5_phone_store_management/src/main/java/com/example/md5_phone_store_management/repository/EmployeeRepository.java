package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeRepository {

    private static final String QUERY_FOR_DELETE_EMPLOYEE = "DELETE FROM employee WHERE employeeID = ?";
    private static final String QUERY_FOR_SELECT_ALL_EMPLOYEES = "SELECT * FROM employee";
    private static final String QUERY_FOR_SELECT_BY_FULLNAME = "SELECT * FROM employee WHERE full_name = ?";
    private static final String QUERY_FOR_SELECT_BY_PHONE = "SELECT * FROM employee WHERE phone = ?";
    private static final String QUERY_FOR_SELECT_BY_EMAIL = "SELECT * FROM employee WHERE email = ?";
    private static final String QUERY_FOR_SELECT_BY_ROLE = "SELECT * FROM employee WHERE role = ?";
    ;

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Employee> employeeRowMapper() {
        return (rs, rowNum) -> {
            Employee employee = new Employee();
            employee.setEmployeeID(rs.getInt("employeeID"));
            employee.setFullName(rs.getString("fullName"));
            employee.setDob(rs.getDate("dob").toLocalDate());
            employee.setAddress(rs.getString("address"));
            employee.setPhone(rs.getString("phone"));
            employee.setUsername(rs.getString("username"));
            employee.setPassword(rs.getString("password"));
            employee.setRole(Role.valueOf(rs.getString("role")));
            employee.setEmail(rs.getString("email"));
            return employee;
        };
    }


    //    tìm all
    public List<Employee> findAll() {
        return jdbcTemplate.query(QUERY_FOR_SELECT_ALL_EMPLOYEES, employeeRowMapper());
    }

    // Tìm theo fullName
    public List<Employee> findByFullName(String fullName) {
        return jdbcTemplate.query(QUERY_FOR_SELECT_BY_FULLNAME,  new Object[]{fullName}, employeeRowMapper());
    }

    // Tìm theo số phone ( ko có thì trả về null )
    public Employee findByPhone(String phone) {
        try {
            return jdbcTemplate.queryForObject(QUERY_FOR_SELECT_BY_PHONE, new Object[]{phone}, employeeRowMapper());
        } catch (Exception e) {
            return null;
        }
    }

    // Tìm theo EMAIL ( ko có thì trả về null )
    public Employee findByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(QUERY_FOR_SELECT_BY_EMAIL, new Object[]{email}, employeeRowMapper());
        } catch (Exception e) {
            return null;
        }
    }

    // Tìm theo Role
    public List<Employee> findByRole(Role role) {
        return jdbcTemplate.query(QUERY_FOR_SELECT_BY_ROLE, new Object[]{role.name()}, employeeRowMapper());
    }


    // xóa với id
    public void deleteById(Long id) {
        int rowsAffected = jdbcTemplate.update(QUERY_FOR_DELETE_EMPLOYEE, id);

        if (rowsAffected > 0) {
            System.out.println("Deleted employee with ID " + id);
        } else {
            System.out.println("No employee found with ID " + id);
        }
    }
}
