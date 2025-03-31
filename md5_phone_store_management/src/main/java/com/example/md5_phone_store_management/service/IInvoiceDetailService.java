package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InvoiceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface IInvoiceDetailService {
    Page<InvoiceDetail> findAll(Pageable pageable);


    Page<InvoiceDetail> sortByCustomerFullName(Pageable pageable);

    Page<InvoiceDetail> sortByProductName(Pageable pageable);

    Page<InvoiceDetail> sortByPrice(Pageable pageable);
}
