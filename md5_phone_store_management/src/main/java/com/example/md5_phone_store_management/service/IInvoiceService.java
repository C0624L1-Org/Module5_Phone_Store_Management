package com.example.md5_phone_store_management.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.md5_phone_store_management.model.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;

public interface IInvoiceService {

    Integer getEmployeeSellingRank(Long employeeID);
    Integer getEmployeeTotalOrdersSold(Long employeeID);
    BigDecimal getEmployeeTotalRevenueSold(Long employeeID);
    Integer getEmployeeOrdersToday(Long employeeID);
    BigDecimal getEmployeeRevenueToday(Long employeeID);
    Integer getEmployeeOrdersThisMonth(Long employeeID);
    BigDecimal getEmployeeRevenueThisMonth(Long employeeID);


    String getBestSalesStaffName();
    Integer getBestSalesStaffSellingQuantity();

    Long totalTodayInvoiceRevenue();


    Long totalThisMonthInvoiceRevenue();
    Long totalLastMonthInvoiceRevenue();


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

    Integer countAllSuccessInvoices();

    Long totalRevenue();

    Integer countTodaySuccessInvoices();

    Integer countThisMonthSuccessInvoices();


    // Lấy tất cả hóa đơn của khách hàng
    List<Invoice> findByCustomer(Customer customer);

    // Lấy tất cả hóa đơn của khách hàng với phân trang
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    // Lấy tất cả hóa đơn của khách hàng theo ID
    List<Invoice> findByCustomerId(Integer customerID);

    // Lấy tất cả hóa đơn của khách hàng theo ID với phân trang
    Page<Invoice> findByCustomerId(Integer customerID, Pageable pageable);

    // Các phương thức cho biểu đồ theo ngày
    List<Object[]> getDailyInvoiceStats();
    List<Object[]> getDailyInvoiceStatsByMonthAndYear(int month, int year);
    
    // Các phương thức cho biểu đồ theo tháng
    List<Object[]> getMonthlyInvoiceStats();
    List<Object[]> getMonthlyInvoiceStatsByYear(int year);
    
    // Phương thức cho biểu đồ theo năm
    List<Object[]> getYearlyInvoiceStats();
    // filter
   // List<Object[]> filterReport(String groupBy, Integer month, Integer year, PaymentMethod paymentMethod,
                               // String productName, String employeeName);
}