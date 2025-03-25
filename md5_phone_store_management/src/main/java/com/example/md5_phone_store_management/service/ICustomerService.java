package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface ICustomerService {
    Page<Customer> findAllCustomers(Pageable pageable);

    Integer countTotalCustomers();

    Page<Customer> searchCustomers(String name, String phone, String gender, Pageable pageable);
}


