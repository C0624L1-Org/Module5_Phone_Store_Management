package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IInvoiceService {
    Invoice saveInvoice(Invoice invoice);
    Invoice findById(Long id);

    // Lấy tất cả hóa đơn của khách hàng
    List<Invoice> findByCustomer(Customer customer);

    // Lấy tất cả hóa đơn của khách hàng với phân trang
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    // Lấy tất cả hóa đơn của khách hàng theo ID
    List<Invoice> findByCustomerId(Integer customerID);

    // Lấy tất cả hóa đơn của khách hàng theo ID với phân trang
    Page<Invoice> findByCustomerId(Integer customerID, Pageable pageable);
}