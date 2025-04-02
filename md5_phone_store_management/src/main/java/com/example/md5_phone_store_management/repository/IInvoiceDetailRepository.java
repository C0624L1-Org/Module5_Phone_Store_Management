package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.InvoiceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IInvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {
    InvoiceDetail save(InvoiceDetail invoiceDetail);

    Page<InvoiceDetail> findAll(Pageable pageable);

    @Query("select  ind from InvoiceDetail ind " +
            "order by  ind.invoice.customer.fullName asc")
    Page<InvoiceDetail> sortByCustomerFullName(Pageable pageable);

    @Query("select  ind from InvoiceDetail ind " +
            "order by  ind.product.name asc")
    Page<InvoiceDetail> sortByProductName(Pageable pageable);

    @Query("select  ind from InvoiceDetail ind " +
            "order by  ind.totalPrice asc")
    Page<InvoiceDetail> sortByPrice(Pageable pageable);

}
