package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.PaymentMethod;
import com.example.md5_phone_store_management.model.dto.SaleReportData;

import java.time.LocalDateTime;
import java.util.List;

public interface ISaleReportService {
    List<Invoice> getFilteredInvoices(PaymentMethod paymentMethod, String employeeName, String productName);

    long getTotalOrders(LocalDateTime startDate, LocalDateTime endDate);

    Long getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);

    long getTotalCustomers(LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> getRevenueByProduct(LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> getRevenueByTime(LocalDateTime startDate, LocalDateTime endDate);

    SaleReportData generateSalesReport(PaymentMethod paymentMethod, String employeeName, String productName);
}
