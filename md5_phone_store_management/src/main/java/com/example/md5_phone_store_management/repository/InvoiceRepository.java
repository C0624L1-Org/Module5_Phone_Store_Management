package com.example.md5_phone_store_management.repository;

import java.util.List;

import com.example.md5_phone_store_management.model.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Sắp xếp
    //Theo thoi gian
    @Query(value = "SELECT * FROM invoices WHERE invoices.status = 'SUCCESS' ORDER BY TIME(created_at) ASC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithTimeAsc(Pageable pageable);
    @Query(value = "SELECT * FROM invoices WHERE invoices.status = 'SUCCESS' ORDER BY TIME(created_at) DESC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithTimeDesc(Pageable pageable);
    //Theo ten khach hang
    @Query(value = "SELECT * FROM invoices LEFT JOIN customer ON invoices.customer_id = customer.customerID WHERE invoices.status = 'SUCCESS' ORDER BY customer.full_name ASC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithCustomerNameAsc(Pageable pageable);
    @Query(value = "SELECT * FROM invoices LEFT JOIN customer ON invoices.customer_id = customer.customerID WHERE invoices.status = 'SUCCESS' ORDER BY customer.full_name DESC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithCustomerNameDesc(Pageable pageable);
    //Theo tên sản phẩm
    @Query(value = "SELECT * FROM invoices i LEFT JOIN (SELECT id.invoice_id, MIN(p.name) as first_product_name FROM invoicedetail id JOIN product p ON id.product_id = p.productID GROUP BY id.invoice_id) as product_info ON i.id = product_info.invoice_id WHERE i.status = 'SUCCESS' ORDER BY product_info.first_product_name ASC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithProductNameAsc(Pageable pageable);
    @Query(value = "SELECT * FROM invoices i LEFT JOIN (SELECT id.invoice_id, MIN(p.name) as first_product_name FROM invoicedetail id JOIN product p ON id.product_id = p.productID GROUP BY id.invoice_id) as product_info ON i.id = product_info.invoice_id WHERE i.status = 'SUCCESS' ORDER BY product_info.first_product_name DESC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithProductNameDesc(Pageable pageable);
    //Theo so tien
    @Query(value = "SELECT * FROM invoices WHERE status = 'SUCCESS' ORDER BY amount ASC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithAmountAsc(Pageable pageable);
    @Query(value = "SELECT * FROM invoices WHERE status = 'SUCCESS' ORDER BY amount DESC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithAmountDesc(Pageable pageable);
    //Theo so luong
    @Query(value = "SELECT i.*, SUM(id.quantity) AS quantity FROM invoices i, invoicedetail id WHERE i.id = id.invoice_id AND i.status = 'SUCCESS' GROUP BY id.invoice_id ORDER BY quantity ASC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithQuantityAsc(Pageable pageable);
    @Query(value = "SELECT i.*, SUM(id.quantity) AS quantity FROM invoices i, invoicedetail id WHERE i.id = id.invoice_id AND i.status = 'SUCCESS' GROUP BY id.invoice_id ORDER BY quantity DESC", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoicesWithQuantityDesc(Pageable pageable);

    //tim tat ca cac hoa don thanh cong
    @Query(value = "SELECT * FROM invoices WHERE status = 'SUCCESS'", nativeQuery = true)
    Page<Invoice> findAllSuccessInvoices(Pageable pageable);


    // Tìm tất cả hóa đơn của một khách hàng
    List<Invoice> findByCustomer(Customer customer);

    // Tìm tất cả hóa đơn của một khách hàng với phân trang
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    // Tìm tất cả hóa đơn của một khách hàng theo ID
    List<Invoice> findByCustomer_CustomerID(Integer customerID);

    // Tìm tất cả hóa đơn của một khách hàng theo ID với phân trang
    Page<Invoice> findByCustomer_CustomerID(Integer customerID, Pageable pageable);

    // Truy vấn cho biểu đồ theo ngày (trong tháng hiện tại)
    @Query(value = "SELECT DAY(created_at) as day, COUNT(id) as invoice_count, SUM(amount) as total_amount " +
           "FROM invoices " +
           "WHERE status = 'SUCCESS' AND MONTH(created_at) = MONTH(CURRENT_DATE()) AND YEAR(created_at) = YEAR(CURRENT_DATE()) " +
           "GROUP BY DAY(created_at) " +
           "ORDER BY day", nativeQuery = true)
    List<Object[]> getDailyInvoiceStats();

    // Truy vấn cho biểu đồ theo ngày trong khoảng thời gian
    @Query(value = "SELECT DAY(created_at) as day, COUNT(id) as invoice_count, SUM(amount) as total_amount " +
           "FROM invoices " +
           "WHERE status = 'SUCCESS' AND MONTH(created_at) = ?1 AND YEAR(created_at) = ?2 " +
           "GROUP BY DAY(created_at) " +
           "ORDER BY day", nativeQuery = true)
    List<Object[]> getDailyInvoiceStatsByMonthAndYear(int month, int year);

    // Truy vấn cho biểu đồ theo tháng (trong năm hiện tại)
    @Query(value = "SELECT MONTH(created_at) as month, COUNT(id) as invoice_count, SUM(amount) as total_amount " +
           "FROM invoices " +
           "WHERE status = 'SUCCESS' AND YEAR(created_at) = YEAR(CURRENT_DATE()) " +
           "GROUP BY MONTH(created_at) " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyInvoiceStats();

    // Truy vấn cho biểu đồ theo tháng trong một năm cụ thể
    @Query(value = "SELECT MONTH(created_at) as month, COUNT(id) as invoice_count, SUM(amount) as total_amount " +
           "FROM invoices " +
           "WHERE status = 'SUCCESS' AND YEAR(created_at) = ?1 " +
           "GROUP BY MONTH(created_at) " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyInvoiceStatsByYear(int year);

    // Truy vấn cho biểu đồ theo năm
    @Query(value = "SELECT YEAR(created_at) as year, COUNT(id) as invoice_count, SUM(amount) as total_amount " +
           "FROM invoices " +
           "WHERE status = 'SUCCESS' " +
           "GROUP BY YEAR(created_at) " +
           "ORDER BY year", nativeQuery = true)
    List<Object[]> getYearlyInvoiceStats();
    // Filter
    // Truy vấn doanh thu theo ngày trong tháng
    @Query("""
        SELECT 
            FUNCTION('DATE', i.createdAt) AS date,
            COUNT(DISTINCT i.id) AS totalInvoice,
            SUM(i.amount) AS totalRevenue
        FROM Invoice i
        JOIN i.invoiceDetailList d
        JOIN d.product p
        LEFT JOIN i.employee e
        WHERE 
            i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS
            AND (:paymentMethod IS NULL OR i.paymentMethod = :paymentMethod)
            AND (:employeeName IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :employeeName, '%')))
            AND (:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%')))
            AND FUNCTION('MONTH', i.createdAt) = :month
            AND FUNCTION('YEAR', i.createdAt) = :year
        GROUP BY FUNCTION('DATE', i.createdAt)
        ORDER BY FUNCTION('DATE', i.createdAt)
    """)
    List<Object[]> getDailyRevenueReport(
            @Param("month") int month,
            @Param("year") int year,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("employeeName") String employeeName,
            @Param("productName") String productName
    );

    // Truy vấn doanh thu theo tháng trong năm
    @Query("""
        SELECT 
            FUNCTION('MONTH', i.createdAt) AS month,
            COUNT(DISTINCT i.id) AS totalInvoice,
            SUM(i.amount) AS totalRevenue
        FROM Invoice i
        JOIN i.invoiceDetailList d
        JOIN d.product p
        LEFT JOIN i.employee e
        WHERE 
            i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS
            AND FUNCTION('YEAR', i.createdAt) = :year
            AND (:paymentMethod IS NULL OR i.paymentMethod = :paymentMethod)
            AND (:employeeName IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :employeeName, '%')))
            AND (:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%')))
        GROUP BY FUNCTION('MONTH', i.createdAt)
        ORDER BY FUNCTION('MONTH', i.createdAt)
    """)
    List<Object[]> getMonthlyRevenueReport(
            @Param("year") int year,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("employeeName") String employeeName,
            @Param("productName") String productName
    );

    // Truy vấn doanh thu theo các năm
    @Query("""
        SELECT 
            FUNCTION('YEAR', i.createdAt) AS year,
            COUNT(DISTINCT i.id) AS totalInvoice,
            SUM(i.amount) AS totalRevenue
        FROM Invoice i
        JOIN i.invoiceDetailList d
        JOIN d.product p
        LEFT JOIN i.employee e
        WHERE 
            i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS
            AND (:paymentMethod IS NULL OR i.paymentMethod = :paymentMethod)
            AND (:employeeName IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :employeeName, '%')))
            AND (:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%')))
        GROUP BY FUNCTION('YEAR', i.createdAt)
        ORDER BY FUNCTION('YEAR', i.createdAt)
    """)
    List<Object[]> getYearlyRevenueReport(
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("employeeName") String employeeName,
            @Param("productName") String productName
    );
}