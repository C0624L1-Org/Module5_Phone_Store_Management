package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.PaymentMethod;
import com.example.md5_phone_store_management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleReportRepository extends JpaRepository<Invoice,Integer> {

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = 'SUCCESS' AND i.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.status = 'SUCCESS' and i.createdAt BETWEEN :startDate AND :endDate")
    Long sumAmountByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT p FROM InvoiceDetail id " +
            "JOIN id.product p " +
            "JOIN id.invoice i " +
            "JOIN i.employee e " +
            "WHERE i.status = 'SUCCESS' " +
            "AND (:paymentMethod IS NULL OR i.paymentMethod = :paymentMethod) " +
            "AND (COALESCE(:employeeName, '') = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :employeeName, '%'))) " +
            "AND (COALESCE(:productName, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%')))")
    List<Product> findPaidProductsWithFilters(
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("employeeName") String employeeName,
            @Param("productName") String productName);

    @Query("SELECT DISTINCT c FROM Invoice i JOIN i.customer c WHERE i.status = 'SUCCESS' and i.createdAt BETWEEN :startDate AND :endDate")
    List<Customer> findDistinctCustomersByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.name, SUM(id.totalPrice) AS totalRevenue " +
            "FROM InvoiceDetail id JOIN id.product p JOIN id.invoice i " +
            "WHERE i.status = 'SUCCESS' and i.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY p.name ORDER BY totalRevenue DESC")
    List<Object[]> sumTotalPriceByProductAndCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query("SELECT FUNCTION('DATE', i.createdAt), SUM(i.amount) " +
            "FROM Invoice i " +
            "WHERE i.status = 'SUCCESS' and i.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', i.createdAt) ORDER BY FUNCTION('DATE', i.createdAt)")
    List<Object[]> sumAmountByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SUCCESS' and " +
            "(:paymentMethod IS NULL OR i.paymentMethod = :paymentMethod) AND " +
            "(COALESCE(:employeeName, '') = '' OR LOWER(i.employee.fullName) LIKE %:employeeName%) AND " +
            "(COALESCE(:productName, '') = '' OR EXISTS (SELECT id FROM i.invoiceDetailList id JOIN id.product p WHERE LOWER(p.name) LIKE %:productName%))")
    List<Invoice> filterInvoices(
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("employeeName") String employeeName,
            @Param("productName") String productName);
}
