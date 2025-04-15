package com.example.md5_phone_store_management.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.IInvoiceRepository;
import com.example.md5_phone_store_management.service.implement.ProductService;

@Service
public class SalesReportService {

    private static final Logger logger = LoggerFactory.getLogger(SalesReportService.class);

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private ProductService productService;

    private static final DateTimeFormatter VNPAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public Map<String, Object> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate, Integer productId) {
        String startDateStr = startDate.format(VNPAY_FORMATTER);
        String endDateStr = endDate.format(VNPAY_FORMATTER);

        logger.info("Querying invoices from " + startDateStr + " to " + endDateStr);

        List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(startDateStr, endDateStr);

        logger.info("Invoices found: " + invoices.size());
        if (invoices.isEmpty()) {
            logger.warn("No invoices found in the given date range.");
            return null;
        }

        if (productId != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getInvoiceDetailList() != null && invoice.getInvoiceDetailList().stream()
                            .anyMatch(detail -> detail.getProduct() != null && detail.getProduct().getProductID().equals(productId)))
                    .collect(Collectors.toList());
            logger.info("Filtered by productId " + productId + ". Invoices remaining: " + invoices.size());
            if (invoices.isEmpty()) {
                logger.warn("No invoices found for productId " + productId);
                return null;
            }
        }

        long totalOrders = invoices.size();
        logger.info("Total Orders: " + totalOrders);

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

        long totalProductsSold = invoices.stream()
                .flatMap(invoice -> {
                    List<InvoiceDetail> details = invoice.getInvoiceDetailList();
                    if (details == null || details.isEmpty()) return Stream.empty();
                    return details.stream();
                })
                .filter(detail -> detail.getQuantity() != null)
                .mapToLong(InvoiceDetail::getQuantity)
                .sum();

        Map<Integer, Long> revenueByProduct = invoices.stream()
                .flatMap(invoice -> {
                    List<InvoiceDetail> details = invoice.getInvoiceDetailList();
                    if (details == null || details.isEmpty()) return Stream.empty();
                    return details.stream();
                })
                .filter(detail -> detail.getProduct() != null && detail.getQuantity() != null)
                .collect(Collectors.groupingBy(
                        detail -> detail.getProduct().getProductID(),
                        Collectors.mapping(
                                detail -> {
                                    BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                    BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                    BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                    return revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                },
                                Collectors.summingLong(Long::longValue)
                        )
                ));

        // Lấy thông tin chi tiết sản phẩm từ ProductService
        Map<Integer, Product> productDetails = revenueByProduct.keySet().stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> productService.findById(id) // Sử dụng ProductService
                ));

        logger.info("Total Revenue: " + totalRevenue);
        logger.info("Total Products Sold: " + totalProductsSold);
        logger.info("Revenue by Product: " + revenueByProduct);
        logger.info("Product Details: " + productDetails);

        Map<String, Object> report = new HashMap<>();
        report.put("totalOrders", totalOrders);
        report.put("totalRevenue", totalRevenue);
        report.put("totalProductsSold", totalProductsSold);
        report.put("revenueByProduct", revenueByProduct);
        report.put("productDetails", productDetails);

        return report;
    }

    public boolean isProductIdValid(Integer productId) {
        if (productId == null) return false;
        return productService.findById(productId) != null;
    }

    public Product findProductById(Integer productId) {
        if (productId == null) return null;
        return productService.findById(productId);
    }
}