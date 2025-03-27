package com.example.md5_phone_store_management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.md5_phone_store_management.model.Customer;

public interface ICustomerService {
    Page<Customer> findAllCustomers(Pageable pageable);

    Integer countTotalCustomers();

    Page<Customer> searchCustomers(String name, String phone, String gender, Pageable pageable);

    Customer findCustomerById(Integer id);

    Customer saveCustomer(Customer customer);

    // Phương thức cập nhật trực tiếp số lần mua hàng
    void updatePurchaseCount(Integer customerId, int newCount);
}


