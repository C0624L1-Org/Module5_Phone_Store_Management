package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
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
    private static final String UPDATE_CUSTOMER = "UPDATE customer SET full_name = ?, phone = ?, address = ?, email = ?, dob = ?, gender = ? WHERE customerID = ?";
    private static final String DELETE_CUSTOMERS_BY_IDS = "DELETE FROM customer WHERE customerID = ?";
    private static final String SELECT_CUSTOMER_BY_ID = "SELECT * FROM customer WHERE customerID = ?";
    private static final String INSERT_CUSTOMER = "INSERT INTO customer (full_name, phone, address, email, dob, gender, purchase_count) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SEARCH_CUSTOMER_BY_NAME = "SELECT * FROM customer WHERE full_name like ?";
    private static final String SEARCH_CUSTOMER_BY_PHONE = "SELECT * FROM customer WHERE phone like ?";
    private static final String SEARCH_CUSTOMER_BY_EMAIL = "SELECT * FROM customer WHERE email like ?";
    private static final String SEARCH_CUSTOMER_BY_GENDER = "SELECT * FROM customer WHERE gender = ?";

    //    searchCustomers(searchType, keyWord);
    public List<Customer> searchCustomers(String searchType, String keyWord) {
        List<Object> params = new ArrayList<>();
        String sql = "";
        if (searchType.equals("name")) {
            sql = SEARCH_CUSTOMER_BY_NAME;
            params.add("%" + keyWord + "%");
        } else if (searchType.equals("phone")) {
            sql = SEARCH_CUSTOMER_BY_PHONE;
            params.add("%" + keyWord + "%");
        } else if (searchType.equals("email")) {
            sql = SEARCH_CUSTOMER_BY_EMAIL;
            params.add("%" + keyWord + "%");
        } else if (searchType.equals("gender")) {
            sql = SEARCH_CUSTOMER_BY_GENDER;
            params.add(keyWord);
        } else {
            sql = "SELECT * FROM customer";
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
            customer.setFullName(rs.getString("full_name"));
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
