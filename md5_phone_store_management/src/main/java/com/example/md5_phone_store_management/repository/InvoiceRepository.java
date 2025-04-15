package com.example.md5_phone_store_management.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.model.PaymentMethod;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Sắp xếp
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS")
    Integer countAllSuccessInvoices();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS AND FUNCTION('DATE', i.createdAt) = :date")
    Integer countSuccessInvoicesByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS AND i.createdAt >= :startDate AND i.createdAt <= :endDate")
    Integer countSuccessInvoicesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);




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
    
    // Tìm tất cả hóa đơn theo trạng thái
    List<Invoice> findByStatus(InvoiceStatus status);
    
    // Tìm tất cả hóa đơn theo trạng thái với phân trang
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    
    // Tìm tất cả hóa đơn theo phương thức thanh toán
    List<Invoice> findByPaymentMethod(PaymentMethod paymentMethod);
    
    // Tìm tất cả hóa đơn theo phương thức thanh toán với phân trang
    Page<Invoice> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    
    // Tìm tất cả hóa đơn theo nhân viên thực hiện
    List<Invoice> findByEmployee(Employee employee);
    
    // Tìm tất cả hóa đơn theo nhân viên thực hiện với phân trang
    Page<Invoice> findByEmployee(Employee employee, Pageable pageable);
    
    // Tìm tất cả hóa đơn theo nhân viên thực hiện theo ID
    List<Invoice> findByEmployee_EmployeeID(Integer employeeID);
    
    // Tìm tất cả hóa đơn theo nhân viên thực hiện theo ID với phân trang
    Page<Invoice> findByEmployee_EmployeeID(Integer employeeID, Pageable pageable);
    
    // Tìm hóa đơn theo khoảng thời gian
    List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Tìm hóa đơn theo khoảng thời gian với phân trang
    Page<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Tìm hóa đơn theo trạng thái và phương thức thanh toán
    List<Invoice> findByStatusAndPaymentMethod(InvoiceStatus status, PaymentMethod paymentMethod);
    
    // Tìm hóa đơn theo trạng thái và phương thức thanh toán với phân trang
    Page<Invoice> findByStatusAndPaymentMethod(InvoiceStatus status, PaymentMethod paymentMethod, Pageable pageable);
    
    // Đếm số hóa đơn theo trạng thái
    Long countByStatus(InvoiceStatus status);
    
    // Đếm số hóa đơn theo phương thức thanh toán
    Long countByPaymentMethod(PaymentMethod paymentMethod);
    
    // Tính tổng doanh thu của tất cả hóa đơn thành công
    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS")
    Long getTotalRevenue();
    
    // Tính tổng doanh thu theo khoảng thời gian
    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.status = com.example.md5_phone_store_management.model.InvoiceStatus.SUCCESS AND i.createdAt BETWEEN ?1 AND ?2")
    Long getTotalRevenueBetween(LocalDateTime startDate, LocalDateTime endDate);
}