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

    @Query("SELECT COALESCE(COUNT(inv), 0) " +
            "FROM Invoice inv " +
            "WHERE inv.customer.customerID = :customerId")
    Integer getTopBuyingCustomerTotalPurchaseQuantity(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(id.quantity), 0) " +
            "FROM Invoice inv " +
            "JOIN inv.invoiceDetailList id " +
            "WHERE inv.employee.employeeID = :employeeId")
    Integer countBestSalesStaffSellingQuantityWithEmployeeId(@Param("employeeId") Integer employeeId);


    @Query("SELECT i FROM Invoice i WHERE i.payDate >= :startDate AND i.payDate <= :endDate")
    List<Invoice> findInvoicesByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // Đếm số đơn hàng trong khoảng thời gian
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.payDate >= :startDate AND i.payDate <= :endDate")
    long countInvoicesByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // Tính tổng doanh thu trong khoảng thời gian
    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.payDate >= :startDate AND i.payDate <= :endDate")
    Long sumAmountByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query("SELECT p FROM Product p WHERE p.productID = :productId")
    Product findProductById(Integer productId);
}