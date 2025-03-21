package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Integer>{
    @Query(value = "SELECT * FROM customer", nativeQuery = true)
    Page<Customer> getAllCustomerPageable(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM customer", nativeQuery = true)
    Integer countTotalCustomers();


//tìm
@Query(value = "SELECT * FROM customer WHERE full_Name LIKE %?1%", nativeQuery = true)
Page<Customer> findByName(String name, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE phone LIKE %?1%", nativeQuery = true)
    Page<Customer> findByPhone(String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE gender = ?1", nativeQuery = true)
    Page<Customer> findByGender(String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE %?1% AND phone LIKE %?2%", nativeQuery = true)
    Page<Customer> findByNameAndPhone(String name, String phone, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE %?1% AND gender = ?2", nativeQuery = true)
    Page<Customer> findByNameAndGender(String name, String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE phone LIKE %?1% AND gender = ?2", nativeQuery = true)
    Page<Customer> findByPhoneAndGender(String phone, String gender, Pageable pageable);

    @Query(value = "SELECT * FROM customer WHERE full_Name LIKE %?1% AND phone LIKE %?2% AND gender = ?3", nativeQuery = true)
    Page<Customer> findByNameAndPhoneAndGender(String name, String phone, String gender, Pageable pageable);
}
