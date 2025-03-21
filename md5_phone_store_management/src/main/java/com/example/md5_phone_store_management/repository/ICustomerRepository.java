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

}
