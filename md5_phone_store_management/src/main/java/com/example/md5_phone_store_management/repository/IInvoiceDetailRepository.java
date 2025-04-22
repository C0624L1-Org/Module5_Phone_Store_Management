package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.InvoiceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
// ko cso tt succsec
//    @Query("SELECT COALESCE(SUM(id.totalPrice), 0) " +
//            "FROM InvoiceDetail id " +
//            "WHERE id.invoice.customer.customerID = :customerId")
//    Long getTopBuyingCustomerTotalPurchaseByTopBuyingCustomerId(@Param("customerId") Long customerId);
//
//
//    @Query("SELECT COALESCE((SELECT id.invoice.customer.customerID " +
//            "FROM InvoiceDetail id " +
//            "WHERE id.invoice.customer.customerID IS NOT NULL " +
//            "GROUP BY id.invoice.customer.customerID " +
//            "ORDER BY SUM(id.totalPrice) DESC " +
//            "LIMIT 1), 0)")
//    Long getTopBuyingCustomerId();
//
//    @Query("SELECT id.invoice.employee.employeeID " +
//            "FROM InvoiceDetail id " +
//            "WHERE id.invoice.employee.employeeID IS NOT NULL " +
//            "GROUP BY id.invoice.employee.employeeID " +
//            "ORDER BY SUM(id.quantity) DESC " +
//            "LIMIT 1")
//    Integer getBestSalesStaffEmployeeId();
//
//    @Query("SELECT COALESCE(SUM(id.quantity), 0) " +
//            "FROM InvoiceDetail id " +
//            "WHERE id.product.productID = :productId")
//    Integer findTopSellingProductNamePurchaseQuantityByProductId(@Param("productId") Long productId);
//
//    @Query("SELECT COALESCE((SELECT id.product.productID " +
//            "FROM InvoiceDetail id " +
//            "GROUP BY id.product.productID " +
//            "ORDER BY COUNT(id.product.productID) DESC " +
//            "LIMIT 1), 0)")
//    Long getTopSellingProductId();
//
//
//    @Query("SELECT COALESCE(SUM(id.totalPrice), 0) FROM InvoiceDetail id WHERE id.invoice.id IN :invoiceIds")
//    Long totalRevenueByInvoiceIds(@Param("invoiceIds") List<Long> invoiceIds);


    @Query("SELECT COALESCE(SUM(id.totalPrice), 0) " +
            "FROM InvoiceDetail id " +
            "WHERE id.invoice.customer.customerID = :customerId AND id.invoice.status = 'SUCCESS'")
    Long getTopBuyingCustomerTotalPurchaseByTopBuyingCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE((SELECT id.invoice.customer.customerID " +
            "FROM InvoiceDetail id " +
            "WHERE id.invoice.customer.customerID IS NOT NULL AND id.invoice.status = 'SUCCESS' " +
            "GROUP BY id.invoice.customer.customerID " +
            "ORDER BY SUM(id.totalPrice) DESC " +
            "LIMIT 1), 0)")
    Long getTopBuyingCustomerId();

    @Query("SELECT id.invoice.employee.employeeID " +
            "FROM InvoiceDetail id " +
            "WHERE id.invoice.employee.employeeID IS NOT NULL AND id.invoice.status = 'SUCCESS' " +
            "GROUP BY id.invoice.employee.employeeID " +
            "ORDER BY SUM(id.quantity) DESC " +
            "LIMIT 1")
    Integer getBestSalesStaffEmployeeId();

    @Query("SELECT COALESCE(SUM(id.quantity), 0) " +
            "FROM InvoiceDetail id " +
            "WHERE id.product.productID = :productId")
    Integer findTopSellingProductNamePurchaseQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE((SELECT id.product.productID " +
            "FROM InvoiceDetail id " +
            "GROUP BY id.product.productID " +
            "ORDER BY COUNT(id.product.productID) DESC " +
            "LIMIT 1), 0)")
    Long getTopSellingProductId();

    @Query("SELECT COALESCE(SUM(id.totalPrice), 0) FROM InvoiceDetail id WHERE id.invoice.id IN :invoiceIds AND id.invoice.status = 'SUCCESS'")
    Long totalRevenueByInvoiceIds(@Param("invoiceIds") List<Long> invoiceIds);















    @Query("SELECT SUM(i.totalPrice) FROM InvoiceDetail i")
    Long totalRevenue();

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
