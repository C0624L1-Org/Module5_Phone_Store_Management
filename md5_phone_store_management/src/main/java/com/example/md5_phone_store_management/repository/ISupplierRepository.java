package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier, Integer> {

    @Query(value = "SELECT * FROM supplier", nativeQuery = true)
    Page<Supplier> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM supplier WHERE supplierID = :id", nativeQuery = true)
    Supplier findBySupplierID(@Param("id") Integer id);

    @Query(value = "SELECT * FROM supplier WHERE " +
            "(name LIKE CONCAT('%', :name, '%') OR :name IS NULL) AND " +
            "(address LIKE CONCAT('%', :address, '%') OR :address IS NULL) AND " +
            "(phone LIKE CONCAT('%', :phone, '%') OR :phone IS NULL) AND " +
            "(email LIKE CONCAT('%', :email, '%') OR :email IS NULL)", nativeQuery = true)
    Page<Supplier> searchSuppliersDynamic(@Param("name") String name, @Param("address") String address,
                                          @Param("phone") String phone, @Param("email") String email, Pageable pageable);

    boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(*) FROM supplier", nativeQuery = true)
    long countSuppliers();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Supplier  s where s.supplierID in :ids ",nativeQuery = true)
    void deleteByIdIn(@Param("ids") List<Integer> ids);
}