package com.example.md5_phone_store_management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.model.Customer;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Integer>{
    @Query(value = "SELECT * FROM customer", nativeQuery = true)
    Page<Customer> getAllCustomerPageable(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM customer", nativeQuery = true)
    Integer countTotalCustomers();

    // Phương thức cập nhật trực tiếp số lần mua hàng
    @Modifying
    @Transactional
    @Query(value = "UPDATE customer SET purchaseCount = :purchaseCount WHERE customerID = :customerId", nativeQuery = true)
    void updatePurchaseCountById(@Param("customerId") Integer customerId, @Param("purchaseCount") int purchaseCount);

    // Phương thức kiểm tra sự tồn tại của phone và email (phục vụ cho annotation Unique)
    @Query(value = "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM customer c WHERE c.phone = :phone AND (c.customerID != :id OR :id IS NULL)", nativeQuery = true)
    boolean isPhoneExistsExceptId(@Param("phone") String phone, @Param("id") Integer id);

    @Query(value = "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM customer c WHERE c.email = :email AND (c.customerID != :id OR :id IS NULL)", nativeQuery = true)
    boolean isEmailExistsExceptId(@Param("email") String email, @Param("id") Integer id);

    // Tìm kiếm với tham số
    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE CONCAT('%',:name,'%')", nativeQuery = true)
    Page<Customer> findByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE phone LIKE CONCAT('%',:phone,'%')", nativeQuery = true)
    Page<Customer> findByPhone(@Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE gender = :gender", nativeQuery = true)
    Page<Customer> findByGender(@Param("gender") String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE CONCAT('%',:name,'%') AND phone LIKE CONCAT('%',:phone,'%')", nativeQuery = true)
    Page<Customer> findByNameAndPhone(@Param("name") String name, @Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE CONCAT('%',:name,'%') AND gender = :gender", nativeQuery = true)
    Page<Customer> findByNameAndGender(@Param("name") String name, @Param("gender") String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE phone LIKE CONCAT('%',:phone,'%') AND gender = :gender", nativeQuery = true)
    Page<Customer> findByPhoneAndGender(@Param("phone") String phone, @Param("gender") String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE CONCAT('%',:name,'%') AND phone LIKE CONCAT('%',:phone,'%') AND gender = :gender", nativeQuery = true)
    Page<Customer> findByNameAndPhoneAndGender(@Param("name") String name, @Param("phone") String phone, @Param("gender") String gender, Pageable pageable);
}
