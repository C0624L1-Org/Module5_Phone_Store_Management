package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SupplierRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // SQL Queries
    private static final String FIND_ALL_QUERY = "SELECT * FROM supplier";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM supplier WHERE supplierID = ?";
    private static final String SAVE_QUERY = "INSERT INTO supplier (name, address, phone, email) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE supplier SET name = ?, address = ?, phone = ?, email = ? WHERE supplierID = ?";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM supplier WHERE supplierID = ?";


    private RowMapper<Supplier> supplierRowMapper = new RowMapper<Supplier>() {
        @Override
        public Supplier mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            Supplier supplier = new Supplier();
            supplier.setSupplierID(resultSet.getInt("supplierID"));
            supplier.setName(resultSet.getString("name"));
            supplier.setAddress(resultSet.getString("address"));
            supplier.setPhone(resultSet.getString("phone"));
            supplier.setEmail(resultSet.getString("email"));
            return supplier;
        }
    };

    public List<Supplier> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, supplierRowMapper);
    }

    public Supplier findById(Integer supplierID) {
        return jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, new Object[]{supplierID}, supplierRowMapper);
    }

    public void save(Supplier supplier) {
        jdbcTemplate.update(SAVE_QUERY, supplier.getName(), supplier.getAddress(), supplier.getPhone(), supplier.getEmail());
    }

    public void update(Integer supplierID, Supplier supplier) {
        jdbcTemplate.update(UPDATE_QUERY, supplier.getName(), supplier.getAddress(), supplier.getPhone(), supplier.getEmail(), supplier.getSupplierID());
    }

    public int deleteById(Integer supplierID) {
        return jdbcTemplate.update(DELETE_BY_ID_QUERY, supplierID);
    }
}
