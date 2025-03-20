//package com.example.md5_phone_store_management.repository;
//
//import com.example.md5_phone_store_management.model.Supplier;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.stereotype.Repository;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public class SupplierRepository {
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    // SQL Queries
//    private static final String FIND_ALL_QUERY = "SELECT * FROM supplier";
//    private static final String FIND_BY_ID_QUERY = "SELECT * FROM supplier WHERE supplierID = ?";
//    private static final String SAVE_QUERY = "INSERT INTO supplier (name, address, phone, email) VALUES (?, ?, ?, ?)";
//    private static final String UPDATE_QUERY = "UPDATE supplier SET name = ?, address = ?, phone = ?, email = ? WHERE supplierID = ?";
//    private static final String DELETE_BY_ID_QUERY = "DELETE FROM supplier WHERE supplierID = ?";
//
//    private final RowMapper<Supplier> supplierRowMapper = (resultSet, rowNum) -> {
//        Supplier supplier = new Supplier();
//        supplier.setSupplierID(resultSet.getInt("supplierID"));
//        supplier.setName(resultSet.getString("name"));
//        supplier.setAddress(resultSet.getString("address"));
//        supplier.setPhone(resultSet.getString("phone"));
//        supplier.setEmail(resultSet.getString("email"));
//        return supplier;
//    };
//
//    public List<Supplier> findAll() {
//        return jdbcTemplate.query(FIND_ALL_QUERY, supplierRowMapper);
//    }
//
//    public Optional<Supplier> findById(Integer supplierID) {
//        List<Supplier> suppliers = jdbcTemplate.query(FIND_BY_ID_QUERY, new Object[]{supplierID}, supplierRowMapper);
//        return suppliers.stream().findFirst();
//    }
//
//    public void save(Supplier supplier) {
//        jdbcTemplate.update(SAVE_QUERY, supplier.getName(), supplier.getAddress(), supplier.getPhone(), supplier.getEmail());
//    }
//
//    public void update(Integer supplierID, Supplier supplier) {
//        jdbcTemplate.update(UPDATE_QUERY, supplier.getName(), supplier.getAddress(), supplier.getPhone(), supplier.getEmail(), supplierID);
//    }
//
//    public void deleteById(Integer supplierID) {
//        jdbcTemplate.update(DELETE_BY_ID_QUERY, supplierID);
//    }
//}
