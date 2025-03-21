package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerService {
    Page<Customer> findAllCustomers(Pageable pageable);
}
