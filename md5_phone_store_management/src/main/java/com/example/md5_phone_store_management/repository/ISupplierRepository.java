package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier, Integer> {

    // Tìm tất cả nhà cung cấp với phân trang
    @Query(value = "SELECT * FROM supplier", nativeQuery = true)
    Page<Supplier> findAll(Pageable pageable);

    // Tìm nhà cung cấp theo ID
    @Query(value = "SELECT * FROM supplier WHERE supplierID = :id", nativeQuery = true)
    Supplier findBySupplierID(Integer id);

    // Tìm kiếm nhà cung cấp động
    @Query(value = "SELECT * FROM supplier WHERE " +
            "(name LIKE %:name% OR :name IS NULL) AND " +
            "(address LIKE %:address% OR :address IS NULL) AND " +
            "(phone LIKE %:phone% OR :phone IS NULL) AND " +
            "(email LIKE %:email% OR :email IS NULL)", nativeQuery = true)
    Page<Supplier> searchSuppliersDynamic(@Param("name") String name, @Param("address") String address,
                                          @Param("phone") String phone, @Param("email") String email, Pageable pageable);

    //Check Email
    boolean existsByEmail(String email);

    // Đếm tổng số nhà cung cấp
    @Query(value = "SELECT COUNT(*) FROM supplier", nativeQuery = true)
    long countSuppliers();
}
