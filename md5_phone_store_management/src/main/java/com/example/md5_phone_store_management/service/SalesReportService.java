package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.repository.IInvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SalesReportService {

    private static final Logger logger = LoggerFactory.getLogger(SalesReportService.class);

    @Autowired
    private IInvoiceRepository invoiceRepository;

    private static final DateTimeFormatter VNPAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public Map<String, Object> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate, String productCode) {
        String startDateStr = startDate.format(VNPAY_FORMATTER);
        String endDateStr = endDate.format(VNPAY_FORMATTER);

        logger.info("Querying invoices from " + startDateStr + " to " + endDateStr);

        // Lấy tất cả hóa đơn trong khoảng thời gian
        List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(startDateStr, endDateStr);

        logger.info("Invoices found: " + invoices.size());
        if (invoices.isEmpty()) {
            logger.warn("No invoices found in the given date range.");
            return null;
        }

        // Nếu có productCode, lọc theo sản phẩm
        if (productCode != null && !productCode.trim().isEmpty()) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getInvoiceDetailList() != null && invoice.getInvoiceDetailList().stream()
                            .anyMatch(detail -> detail.getProduct() != null && productCode.equals(String.valueOf(detail.getProduct().getProductID()))))
                    .collect(Collectors.toList());
            logger.info("Filtered by productCode " + productCode + ". Invoices remaining: " + invoices.size());
        }

        // Tính tổng số đơn hàng
        long totalOrders = invoices.size();
        logger.info("Total Orders: " + totalOrders);

        // Tính tổng doanh thu từ InvoiceDetail
        long totalRevenue = invoices.stream()
                .flatMap(invoice -> {
                    List<InvoiceDetail> details = invoice.getInvoiceDetailList();
                    if (details == null || details.isEmpty()) {
                        logger.warn("Invoice ID " + invoice.getId() + " has no details.");
                        return Stream.empty();
                    }
                    return details.stream();
                })
                .filter(detail -> {
                    boolean valid = detail.getProduct() != null && detail.getQuantity() != null;
                    if (!valid) logger.warn("Invalid detail: " + detail);
                    return valid;
                })
                .mapToLong(detail -> {
                    BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                    BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                    BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                    long revenue = revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                    logger.debug("Detail: Product ID " + detail.getProduct().getProductID() + ", Revenue: " + revenue);
                    return revenue;
                })
                .sum();

        long totalProfit = totalRevenue;

        // Tính doanh thu theo khách hàng
        Map<Long, Long> revenueByCustomer = invoices.stream()
                .filter(invoice -> invoice.getCustomer() != null)
                .collect(Collectors.groupingBy(
                        invoice -> invoice.getCustomer().getCustomerID().longValue(),
                        Collectors.summingLong(invoice -> {
                            List<InvoiceDetail> details = invoice.getInvoiceDetailList();
                            if (details == null || details.isEmpty()) return 0L;
                            return details.stream()
                                    .filter(detail -> detail.getProduct() != null && detail.getQuantity() != null)
                                    .mapToLong(detail -> {
                                        BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                        BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                        BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                        return revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                    })
                                    .sum();
                        })
                ));

        Map<Long, Long> profitByCustomer = revenueByCustomer;

        logger.info("Total Revenue: " + totalRevenue);
        logger.info("Total Profit: " + totalProfit);
        logger.info("Revenue by Customer: " + revenueByCustomer);
        logger.info("Profit by Customer: " + profitByCustomer);

        Map<String, Object> report = new HashMap<>();
        report.put("totalOrders", totalOrders);
        report.put("totalRevenue", totalRevenue);
        report.put("totalProfit", totalProfit);
        report.put("revenueByCustomer", revenueByCustomer);
        report.put("profitByCustomer", profitByCustomer);

        return report;
    }

    public boolean isProductCodeValid(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }
        try {
            Integer productId = Integer.parseInt(productCode);
            List<Invoice> allInvoices = invoiceRepository.findAll();
            return allInvoices.stream()
                    .flatMap(invoice -> invoice.getInvoiceDetailList().stream())
                    .anyMatch(detail -> detail.getProduct() != null && detail.getProduct().getProductID() != null && detail.getProduct().getProductID().equals(productId));
        } catch (NumberFormatException e) {
            logger.error("Invalid product code format: " + productCode);
            return false;
        }
    }
}