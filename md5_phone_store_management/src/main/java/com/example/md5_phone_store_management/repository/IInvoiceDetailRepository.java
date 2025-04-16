package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.InvoiceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;

@Repository
public interface IInvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {

    @Query("SELECT id FROM InvoiceDetail id WHERE id.invoice.id = :invoiceId")
    List<InvoiceDetail> findInvoiceDetailsByInvoiceId(@Param("invoiceId") Long invoiceId);

    // Or if you want to find a single detail by its own ID
    InvoiceDetail findInvoiceDetailById(Long id);


    // Lưu chi tiết hóa đơn
    InvoiceDetail save(InvoiceDetail invoiceDetail);

    // Tìm tất cả chi tiết của một hóa đơn
    List<InvoiceDetail> findByInvoice(Invoice invoice);

    // Tìm tất cả chi tiết của một hóa đơn với phân trang
    Page<InvoiceDetail> findByInvoice(Invoice invoice, Pageable pageable);

    // Tìm tất cả chi tiết của một hóa đơn theo ID
    List<InvoiceDetail> findByInvoice_Id(Long invoiceId);

    // Tính tổng số lượng sản phẩm trong một hóa đơn
    @Query("SELECT SUM(id.quantity) FROM InvoiceDetail id WHERE id.invoice.id = ?1")
    Integer getTotalQuantityByInvoiceId(Long invoiceId);

    // Tính tổng giá trị của một hóa đơn
    @Query("SELECT SUM(id.totalPrice) FROM InvoiceDetail id WHERE id.invoice.id = ?1")
    Long getTotalAmountByInvoiceId(Long invoiceId);

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
