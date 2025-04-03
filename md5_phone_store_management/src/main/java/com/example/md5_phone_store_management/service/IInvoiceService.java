package com.example.md5_phone_store_management.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.model.PaymentMethod;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

public interface IInvoiceService {
    Invoice saveInvoice(Invoice invoice);
    Invoice findById(Long id);
    List<Invoice> findAll();
    Page<Invoice> findAll(Pageable pageable);
    void deleteInvoice(Long id);

    // Sắp xếp
    //Theo thoi gian
    Page<Invoice> findAllSuccessInvoicesWithTimeAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithTimeDesc(Pageable pageable);

    //Theo ten khach hang
    Page<Invoice> findAllSuccessInvoicesWithCustomerNameAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithCustomerNameDesc(Pageable pageable);

    //Theo tên sản phẩm
    Page<Invoice> findAllSuccessInvoicesWithProductNameAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithProductNameDesc(Pageable pageable);

    //Theo so tien
    Page<Invoice> findAllSuccessInvoicesWithAmountAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithAmountDesc(Pageable pageable);

    //Theo so luong
    Page<Invoice> findAllSuccessInvoicesWithQuantityAsc(Pageable pageable);
    Page<Invoice> findAllSuccessInvoicesWithQuantityDesc(Pageable pageable);

    //lay cac hoa don thanh cong
    Page<Invoice> findAllSuccessInvoices(Pageable pageable);

    // Lấy tất cả hóa đơn của khách hàng
    List<Invoice> findByCustomer(Customer customer);

    // Lấy tất cả hóa đơn của khách hàng với phân trang
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    // Lấy tất cả hóa đơn của khách hàng theo ID
    List<Invoice> findByCustomerId(Integer customerID);

    // Lấy tất cả hóa đơn của khách hàng theo ID với phân trang
    Page<Invoice> findByCustomerId(Integer customerID, Pageable pageable);
    
    // Các phương thức mới cho status
    List<Invoice> findByStatus(InvoiceStatus status);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    Invoice updateStatus(Long invoiceId, InvoiceStatus status);
    
    // Các phương thức mới cho paymentMethod
    List<Invoice> findByPaymentMethod(PaymentMethod paymentMethod);
    Page<Invoice> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    
    // Các phương thức mới cho employee
    List<Invoice> findByEmployee(Employee employee);
    Page<Invoice> findByEmployee(Employee employee, Pageable pageable);
    List<Invoice> findByEmployeeId(Integer employeeID);
    Page<Invoice> findByEmployeeId(Integer employeeID, Pageable pageable);
    Invoice assignEmployee(Long invoiceId, Integer employeeId);
    
    // Các phương thức tìm kiếm theo ngày
    List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Các phương thức tìm kiếm kết hợp
    List<Invoice> findByStatusAndPaymentMethod(InvoiceStatus status, PaymentMethod paymentMethod);
    Page<Invoice> findByStatusAndPaymentMethod(InvoiceStatus status, PaymentMethod paymentMethod, Pageable pageable);
    
    // Các phương thức thống kê
    Long countByStatus(InvoiceStatus status);
    Long countByPaymentMethod(PaymentMethod paymentMethod);
    Long getTotalRevenue();
    Long getTotalRevenueBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Phương thức xử lý thanh toán
    Invoice processPayment(Long invoiceId, PaymentMethod paymentMethod);
    Invoice completeInvoice(Long invoiceId, Integer employeeId);
}