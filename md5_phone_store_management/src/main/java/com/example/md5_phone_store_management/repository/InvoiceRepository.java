package com.example.md5_phone_store_management.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.model.PaymentMethod;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Tìm tất cả các hóa đơn thành công
    @Query(value = "SELECT * FROM invoices WHERE invoices.status = 'SUCCESS'", nativeQuery = true)
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