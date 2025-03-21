package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
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
    private static final String INSERT_CUSTOMER = "INSERT INTO customer (full_Name, phone, address, email, dob, gender, purchaseCount) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SEARCH_CUSTOMER_BY_NAME = "SELECT * FROM customer WHERE full_Name like ?";
    private static final String SEARCH_CUSTOMER_BY_PHONE = "SELECT * FROM customer WHERE phone like ?";
    private static final String SEARCH_CUSTOMER_BY_EMAIL = "SELECT * FROM customer WHERE email like ?";
    private static final String SEARCH_CUSTOMER_BY_GENDER = "SELECT * FROM customer WHERE gender = ?";

    // Kiểm tra số điện thoại đã tồn tại
    public boolean isPhoneExists(String phone) {
        String sql = "SELECT COUNT(*) > 0 FROM customer WHERE phone = ?";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, phone));
    }

    // Kiểm tra email đã tồn tại
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) > 0 FROM customer WHERE email = ?";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }

    // Kiểm tra số điện thoại đã tồn tại ngoại trừ ID hiện tại
    public boolean isPhoneExistsExceptId(String phone, Integer id) {
        String sql = "SELECT COUNT(*) > 0 FROM customer WHERE phone = ? AND (customerID != ? OR ? IS NULL)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, phone, id, id));
    }

    // Kiểm tra email đã tồn tại ngoại trừ ID hiện tại
    public boolean isEmailExistsExceptId(String email, Integer id) {
        String sql = "SELECT COUNT(*) > 0 FROM customer WHERE email = ? AND (customerID != ? OR ? IS NULL)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email, id, id));
    }


    public Customer saveAjax(Customer customer) {
        jdbcTemplate.update(INSERT_CUSTOMER, customer.getFullName(), customer.getPhone(), customer.getAddress(), customer.getEmail(), customer.getDob(), customer.getGender().name(), customer.getPurchaseCount());
        // Lấy ID của khách hàng vừa được thêm (nếu có)
        // Giả sử bạn có một cách để lấy ID của khách hàng vừa thêm, ví dụ như sử dụng một truy vấn để lấy khách hàng mới nhất
        // Hoặc bạn có thể thay đổi cách lưu để trả về ID của khách hàng mới
        Integer newCustomerId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        customer.setCustomerID(newCustomerId);
        return customer;
    }



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
