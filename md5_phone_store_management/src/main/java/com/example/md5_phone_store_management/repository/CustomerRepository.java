package com.example.md5_phone_store_management.repository;

import java.security.PublicKey;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;

@Repository
public class CustomerRepository  {
    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL_CUSTOMERS = "SELECT * FROM customer";
    private static final String UPDATE_CUSTOMER = "UPDATE customer SET full_Name = ?, phone = ?, address = ?, email = ?, dob = ?, gender = ? WHERE customerID = ?";
    private static final String DELETE_CUSTOMERS_BY_IDS = "DELETE FROM customer WHERE customerID = ?";
    private static final String DELETE_INVOICES_BY_CUSTOMER_ID = "DELETE FROM invoices WHERE customer_id = ?";
    private static final String DELETE_INVOICE_DETAILS_BY_INVOICE_IDS = "DELETE FROM invoicedetail WHERE invoice_id IN (SELECT id FROM invoices WHERE customer_id = ?)";
    private static final String SELECT_CUSTOMER_BY_ID = "SELECT * FROM customer WHERE customerID = ?";
    private static final String INSERT_CUSTOMER = "INSERT INTO customer (full_Name, phone, address, email, dob, gender, purchaseCount) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_CUSTOMERS_WITH_PURCHASES = "SELECT * FROM customer WHERE purchaseCount > 0 ORDER BY purchaseCount DESC";



    public List<Customer> findAllById(Iterable<Integer> customerIDs) {
        if (customerIDs == null || !customerIDs.iterator().hasNext()) {
            return new ArrayList<>();
        }

        // Convert Iterable to List for SQL IN clause
        List<Integer> idList = new ArrayList<>();
        customerIDs.forEach(idList::add);

        // Create placeholders for the IN clause
        String placeholders = idList.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));
        String sql = "SELECT customerID, full_name, phone, address, email, dob, gender, purchaseCount " +
                "FROM customer WHERE customerID IN (" + placeholders + ")";

        return jdbcTemplate.query(sql, idList.toArray(), new CustomerRowMapper());
    }

    public void deleteCustomer(List<Integer> customerIDs) {
//        xóa hd ct
        jdbcTemplate.batchUpdate(DELETE_INVOICE_DETAILS_BY_INVOICE_IDS, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, customerIDs.get(i));
            }

            @Override
            public int getBatchSize() {
                return customerIDs.size();
            }
        });

        // xóa hd
        jdbcTemplate.batchUpdate(DELETE_INVOICES_BY_CUSTOMER_ID, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, customerIDs.get(i));
            }

            @Override
            public int getBatchSize() {
                return customerIDs.size();
            }
        });

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



    //    List<Customer> customers = customerService.searchCustomers(name, phone, gender);
    public List<Customer> searchCustomers(String name, String phone, String gender) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM customer WHERE 1=1");

        if (name != null && !name.isEmpty()) {
            sql.append(" AND full_Name LIKE ?");
            params.add("%" + name + "%");
        }

        if (phone != null && !phone.isEmpty()) {
            sql.append(" AND phone LIKE ?");
            params.add("%" + phone + "%");
        }

        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND gender = ?");
            params.add(gender);
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), new CustomerRowMapper());
    }

    // Kiểm tra số điện thoại đã tồn tại ngoại trừ ID hiện tại
    public Long isPhoneExistsExceptId(String phone, Integer id) {
        String sql = "SELECT COUNT(*) FROM customer WHERE phone = ? AND (customerID != ? OR ? IS NULL)";
        return jdbcTemplate.queryForObject(sql, Long.class, phone, id, id);
    }

    // Kiểm tra email đã tồn tại ngoại trừ ID hiện tại
    public Long isEmailExistsExceptId(String email, Integer id) {
        String sql = "SELECT COUNT(*) FROM customer WHERE email = ? AND (customerID != ? OR ? IS NULL)";
        return jdbcTemplate.queryForObject(sql, Long.class, email, id, id);
    }
    
    // Lấy danh sách khách hàng có purchaseCount > 0
    public List<Customer> findCustomersWithPurchases() {
        System.out.println("Gọi findCustomersWithPurchases trong CustomerRepository");
        try {
            List<Customer> customers = jdbcTemplate.query(SELECT_CUSTOMERS_WITH_PURCHASES, new CustomerRowMapper());
            System.out.println("Tìm thấy " + customers.size() + " khách hàng có purchaseCount > 0");
            return customers;
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm khách hàng có purchaseCount > 0: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
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

    public Customer save(Customer customer) {
        if (customer.getCustomerID() != null) {
            updateCustomer(customer); // Cập nhật nếu đã có ID
        } else {
            jdbcTemplate.update(INSERT_CUSTOMER, customer.getFullName(), customer.getPhone(), customer.getAddress(), customer.getEmail(), customer.getDob(), customer.getGender().name(), customer.getPurchaseCount());
        }
        return customer;
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
