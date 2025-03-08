package com.example.last.repository;

import com.example.last.model.Customer;
import com.example.last.model.Gender;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL_CUSTOMERS = "SELECT * FROM customer";
    private static final String UPDATE_CUSTOMER = "UPDATE customer SET full_Name = ?, phone = ?, address = ?, email = ?, dob = ?, gender = ? WHERE customerID = ?";
    private static final String DELETE_CUSTOMERS_BY_IDS = "DELETE FROM customer WHERE customerID = ?";
    private static final String SELECT_CUSTOMER_BY_ID = "SELECT * FROM customer WHERE customerID = ?";
    private static final String INSERT_CUSTOMER = "INSERT INTO customer (full_Name, phone, address, email, dob, gender, purchase_count) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SEARCH_CUSTOMER_BY_GENDER = "SELECT * FROM customer WHERE gender = ?";
    private static final String SEARCH_CUSTOMER_BY_NAME_AND_GENDER = "SELECT * FROM customer WHERE full_Name LIKE ? AND gender = ?";
    private static final String SEARCH_CUSTOMER_BY_PHONE_AND_GENDER = "SELECT * FROM customer WHERE phone LIKE ? AND gender = ?";
    private static final String SEARCH_CUSTOMER_BY_EMAIL_AND_GENDER = "SELECT * FROM customer WHERE email LIKE ? AND gender = ?";
    private static final String SEARCH_CUSTOMER_BY_NAME_AND_PHONE_AND_GENDER = "SELECT * FROM customer WHERE full_Name LIKE ? AND phone LIKE ? AND gender = ?";
    private static final String SEARCH_CUSTOMER_BY_NAME_AND_EMAIL_AND_GENDER = "SELECT * FROM customer WHERE full_Name LIKE ? AND email LIKE ? AND gender = ?";
    private static final String SEARCH_CUSTOMER_BY_PHONE_AND_EMAIL_AND_GENDER = "SELECT * FROM customer WHERE phone LIKE ? AND email LIKE ? AND gender = ?";
    private static final String SEARCH_CUSTOMER_BY_ALL_AND_GENDER = "SELECT * FROM customer WHERE full_Name LIKE ? AND phone LIKE ? AND email LIKE ? AND gender = ?";


    public List<Customer> searchCustomers(String name, String phone, String email, String gender) {
        List<Object> params = new ArrayList<>();
        String sql = "";

        if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty() && email != null && !email.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_ALL_AND_GENDER;
            params.add("%" + name + "%");
            params.add("%" + phone + "%");
            params.add("%" + email + "%");
            params.add(gender);
        } else if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_NAME_AND_PHONE_AND_GENDER;
            params.add("%" + name + "%");
            params.add("%" + phone + "%");
            params.add(gender);
        } else if (name != null && !name.isEmpty() && email != null && !email.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_NAME_AND_EMAIL_AND_GENDER;
            params.add("%" + name + "%");
            params.add("%" + email + "%");
            params.add(gender);
        } else if (phone != null && !phone.isEmpty() && email != null && !email.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_PHONE_AND_EMAIL_AND_GENDER;
            params.add("%" + phone + "%");
            params.add("%" + email + "%");
            params.add(gender);
        } else if (name != null && !name.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_NAME_AND_GENDER;
            params.add("%" + name + "%");
            params.add(gender);
        } else if (phone != null && !phone.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_PHONE_AND_GENDER;
            params.add("%" + phone + "%");
            params.add(gender);
        } else if (email != null && !email.isEmpty() && gender != null) {
            sql = SEARCH_CUSTOMER_BY_EMAIL_AND_GENDER;
            params.add("%" + email + "%");
            params.add(gender);
        } else if (gender != null) {
            sql = SEARCH_CUSTOMER_BY_GENDER;
            params.add(gender);
        } else {
            sql = SELECT_ALL_CUSTOMERS;
        }

        return jdbcTemplate.query(sql, params.toArray(), new CustomerRowMapper());
    }

    public void deleteCustomer(List<Integer> customerIDs) {
        jdbcTemplate.batchUpdate(DELETE_CUSTOMERS_BY_IDS, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, customerIDs.get(i));
            }

            @Override
            public int getBatchSize() {
                return customerIDs.size();
            }
        });
    }


    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Customer> findAll() {
        return jdbcTemplate.query(SELECT_ALL_CUSTOMERS, new CustomerRowMapper());
    }

    public void updateCustomer(Customer customer) {
        jdbcTemplate.update(UPDATE_CUSTOMER, customer.getFullName(), customer.getPhone(), customer.getAddress(), customer.getEmail(), customer.getDob(), customer.getGender().name(), customer.getCustomerID());
    }

    public Customer findById(Integer customerID) {
        return jdbcTemplate.queryForObject(SELECT_CUSTOMER_BY_ID, new Object[]{customerID}, new CustomerRowMapper());
    }

    public void save(Customer customer) {
        if (customer.getCustomerID() != null) {
            updateCustomer(customer); // Cập nhật nếu đã có ID
        } else {
            jdbcTemplate.update(INSERT_CUSTOMER, customer.getFullName(), customer.getPhone(), customer.getAddress(), customer.getEmail(), customer.getDob(), customer.getGender().name(), customer.getPurchaseCount());
        }
    }


    //  ánh xạ kết quả từ ResultSet to obj Customer
    private static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Customer customer = new Customer();
            customer.setCustomerID(rs.getInt("customerID"));
            customer.setFullName(rs.getString("full_Name"));
            customer.setPhone(rs.getString("phone"));
            customer.setAddress(rs.getString("address"));
            customer.setEmail(rs.getString("email"));
            customer.setDob(rs.getDate("dob"));
            customer.setGender(Gender.valueOf(rs.getString("gender")));
            customer.setPurchaseCount(rs.getInt("purchaseCount"));
            return customer;
        }
    }
}