package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.model.Customer;

import java.util.List;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Integer>{
    @Query("SELECT c FROM Customer c ORDER BY c.customerID")
    Page<Customer> getAllCustomerPageable(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM customer", nativeQuery = true)
    Integer countTotalCustomers();

    // Phương thức cập nhật trực tiếp số lần mua hàng
    @Modifying
    @Transactional
    @Query(value = "UPDATE customer SET purchaseCount = :purchaseCount WHERE customerID = :customerId", nativeQuery = true)
    void updatePurchaseCountById(@Param("customerId") Integer customerId, @Param("purchaseCount") int purchaseCount);

    // Phương thức kiểm tra sự tồn tại của phone và email (phục vụ cho annotation Unique)
    @Query(value = "SELECT EXISTS(SELECT 1 FROM customer WHERE phone = :phone AND (customerID != :id OR :id IS NULL))", nativeQuery = true)
    boolean isPhoneExistsExceptId(@Param("phone") String phone, @Param("id") Integer id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM customer WHERE email = :email AND (customerID != :id OR :id IS NULL))", nativeQuery = true)
    boolean isEmailExistsExceptId(@Param("email") String email, @Param("id") Integer id);

    // Tìm kiếm với tham số cho tất cả customer
    @Query(value = "SELECT * FROM customer WHERE LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%'))", nativeQuery = true)
    Page<Customer> findByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE phone LIKE CONCAT('%',:phone,'%')", nativeQuery = true)
    Page<Customer> findByPhone(@Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE gender = :gender", nativeQuery = true)
    Page<Customer> findByGender(@Param("gender") String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE LOWER(email) LIKE LOWER(CONCAT('%',:email,'%'))", nativeQuery = true)
    Page<Customer> findByEmail(@Param("email") String email, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%')) AND phone LIKE CONCAT('%',:phone,'%')", nativeQuery = true)
    Page<Customer> findByNameAndPhone(@Param("name") String name, @Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%')) AND gender = :gender", nativeQuery = true)
    Page<Customer> findByNameAndGender(@Param("name") String name, @Param("gender") String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE phone LIKE CONCAT('%',:phone,'%') AND gender = :gender", nativeQuery = true)
    Page<Customer> findByPhoneAndGender(@Param("phone") String phone, @Param("gender") String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%')) AND phone LIKE CONCAT('%',:phone,'%') AND gender = :gender", nativeQuery = true)
    Page<Customer> findByNameAndPhoneAndGender(@Param("name") String name, @Param("phone") String phone, @Param("gender") String gender, Pageable pageable);

    // Tìm kiếm với tham số cho customer với purchaseCount > 0
    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 ORDER BY customerID", nativeQuery = true)
    Page<Customer> findCustomersWithPurchases(Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%'))", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND phone LIKE CONCAT('%',:phone,'%')", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByPhone(@Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND LOWER(email) LIKE LOWER(CONCAT('%',:email,'%'))", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByEmail(@Param("email") String email, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%')) AND phone LIKE CONCAT('%',:phone,'%')", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByNameAndPhone(@Param("name") String name, @Param("phone") String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%')) AND LOWER(email) LIKE LOWER(CONCAT('%',:email,'%'))", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByNameAndEmail(@Param("name") String name, @Param("email") String email, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND phone LIKE CONCAT('%',:phone,'%') AND LOWER(email) LIKE LOWER(CONCAT('%',:email,'%'))", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByPhoneAndEmail(@Param("phone") String phone, @Param("email") String email, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE purchaseCount > 0 AND LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%')) AND phone LIKE CONCAT('%',:phone,'%') AND LOWER(email) LIKE LOWER(CONCAT('%',:email,'%'))", nativeQuery = true)
    Page<Customer> findCustomersWithPurchasesByNameAndPhoneAndEmail(@Param("name") String name, @Param("phone") String phone, @Param("email") String email, Pageable pageable);

    // check email
    boolean existsByEmail(String email);

    // check phone
    boolean existsByPhone(String phone);

    // Đếm số người đã mua hàng
    @Query(value = "SELECT COUNT(*) FROM customer WHERE purchaseCount > 0", nativeQuery = true)
    Integer countCustomersWithPurchases();

    // Đếm số người đã mua hàng với tên khách hàng
    @Query(value = "SELECT COUNT(*) FROM customer WHERE purchaseCount > 0 AND LOWER(full_Name) LIKE LOWER(CONCAT('%',:name,'%'))", nativeQuery = true)
    Integer countCustomersWithPurchasesByName(@Param("name") String name);
    
    // Đồng bộ hóa số lượng mua hàng với dữ liệu thực tế từ bảng invoice
    @Modifying
    @Transactional
    @Query(value = 
        "UPDATE customer c " +
        "SET c.purchaseCount = (SELECT COUNT(DISTINCT i.id) FROM invoice i WHERE i.customer_id = c.customerID AND i.status = 'COMPLETED') " +
        "WHERE c.customerID > 0", 
        nativeQuery = true)
    int synchronizePurchaseCountsFromInvoices();
    
    // Đồng bộ hóa số lượng mua hàng cho một khách hàng cụ thể
    @Modifying
    @Transactional
    @Query(value = 
        "UPDATE customer c " +
        "SET c.purchaseCount = (SELECT COUNT(DISTINCT i.id) FROM invoice i WHERE i.customer_id = c.customerID AND i.status = 'COMPLETED') " +
        "WHERE c.customerID = :customerId", 
        nativeQuery = true)
    int synchronizePurchaseCountForCustomer(@Param("customerId") Integer customerId);
    @Query(value = "SELECT * from customer c WHERE (:gender IS NULL OR LOWER(c.gender) = LOWER(:gender)) " +
            "AND (:age IS NULL OR YEAR(CURDATE()) - YEAR(c.dob) = :age) " +
            "AND (:minPurchaseCount IS NULL OR c.purchaseCount >= :minPurchaseCount)", nativeQuery = true)
    List<Customer> findByFilters(
            @Param("gender") String gender,
            @Param("age") Integer age,
            @Param("minPurchaseCount") Integer minPurchaseCount
    );

}
