package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.md5_phone_store_management.model.Customer;

import java.util.List;

public interface ICustomerService {

    Integer countNewCustomers();

    Integer countMaleCustomers();

    Integer countFemaleCustomers();

    Page<Customer> findAllCustomers(Pageable pageable);

    Integer countTotalCustomers();

    Integer countCustomersWithPurchases();

    Page<Customer> searchCustomers(String name, String phone, String gender, Pageable pageable);

    Customer findCustomerById(Integer id);

    // Phương thức cập nhật trực tiếp số lần mua hàng
    void updatePurchaseCount(Integer customerId, int newCount);

    // customer có purchaseCount > 0
    Page<Customer> findCustomersWithPurchases(Pageable pageable);

    Page<Customer> searchCustomerWithPurchases(String name, String phone, String email, Pageable pageable);

    boolean isEmailExists(String email);

    boolean isPhoneExists(String phone);
    List<Customer> filterCustomers(Gender gender, Integer age, Integer minPurchaseCount);

}


