package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Supplier;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier,Integer> {
    @Query(value = "SELECT * FROM  Supplier ",nativeQuery = true )
    List<Supplier> findAll();

    @Query(value = "SELECT * FROM supplier WHERE supplierID = :id", nativeQuery = true)
    Supplier findBySupplierID( Integer id);

    @Modifying  // Bắt buộc khi dùng INSERT, UPDATE, DELETE
    @Transactional  // Đảm bảo câu lệnh được thực thi trong một transaction
    @Query(value = "INSERT INTO supplier (name, address, phone, email) " +
            "VALUES (:name, :address, :phone, :email)", nativeQuery = true)
    void insert(@Param("name") String name, @Param("address") String address,
                @Param("phone") String phone, @Param("email") String email);
}
