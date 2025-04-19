package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.InvoiceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IInvoiceDetailService {

//    response.put("topBuyingCustomerName", invoiceDetailService.getTopBuyingCustomerName());
//        response.put("topBuyingCustomerTotalPurchase", invoiceDetailService.getTopBuyingCustomerTotalPurchase());
//        response.put("topBuyingCustomerTotalPurchaseQuantity", invoiceDetailService.getTopBuyingCustomerTotalPurchaseQuantity());

    String getTopBuyingCustomerName();

    Long getTopBuyingCustomerTotalPurchase();

    Integer getTopBuyingCustomerTotalPurchaseQuantity();


    String getTopSellingProductName();
    Page<InvoiceDetail> findAll(Pageable pageable);

    Integer getTopSellingProductNamePurchaseQuantity();

    List<InvoiceDetail> findInvoiceDetailById(Long invoiceId);

    Page<InvoiceDetail> sortByCustomerFullName(Pageable pageable);

    Page<InvoiceDetail> sortByProductName(Pageable pageable);

    Page<InvoiceDetail> sortByPrice(Pageable pageable);
}
