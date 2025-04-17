package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE i.payDate >= :startDate AND i.payDate <= :endDate")
    List<Invoice> findInvoicesByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

}