package com.example.md5_phone_store_management.service.implement;


import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.PaymentMethod;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.dto.SaleReportData;
import com.example.md5_phone_store_management.repository.SaleReportRepository;
import com.example.md5_phone_store_management.service.ISaleReportService;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class SaleReportServiceImpl implements ISaleReportService {
    private final SaleReportRepository saleReportRepository;
    private final ProductService productService;
    @Autowired
    public SaleReportServiceImpl(SaleReportRepository saleReportRepository, ProductService productService) {
        this.saleReportRepository = saleReportRepository;
        this.productService = productService;
    }
    // Khai báo Logger
    private static final Logger logger = LoggerFactory.getLogger(SaleReportServiceImpl.class);

    public List<Invoice> getFilteredInvoices(PaymentMethod paymentMethod, String employeeName, String productName) {
        return saleReportRepository.filterInvoices(paymentMethod, employeeName, productName);
    }

    public long getTotalOrders(LocalDateTime startDate, LocalDateTime endDate) {
        return saleReportRepository.countByCreatedAtBetween(startDate, endDate);
    }

    public Long getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return saleReportRepository.sumAmountByCreatedAtBetween(startDate, endDate);
    }

    public long getTotalCustomers(LocalDateTime startDate, LocalDateTime endDate) {
        return saleReportRepository.findDistinctCustomersByCreatedAtBetween(startDate, endDate).size();
    }

    public List<Object[]> getRevenueByProduct(LocalDateTime startDate, LocalDateTime endDate) {
        return saleReportRepository.sumTotalPriceByProductAndCreatedAtBetween(startDate, endDate);
    }

    public List<Object[]> getRevenueByTime(LocalDateTime startDate, LocalDateTime endDate) {
        return saleReportRepository.sumAmountByDate(startDate, endDate);
    }

    public List<Product> getListProduct(PaymentMethod paymentMethod, String employeeName, String productName) {
        return saleReportRepository.findPaidProductsWithFilters(paymentMethod, employeeName, productName);
    }

    public SaleReportData generateSalesReport(PaymentMethod paymentMethod, String employeeName, String productName) {
        List<Invoice> invoices = getFilteredInvoices(paymentMethod, employeeName, productName);
        long totalOrders = invoices.size();
        long totalCustomers = invoices.stream().map(invoice -> invoice.getCustomer().getCustomerID()).distinct().count();
        Long totalRevenue = invoices.stream().mapToLong(Invoice::getAmount).sum();
        List<Object[]> revenueByProduct = invoices.stream()
                .flatMap(invoice -> invoice.getInvoiceDetailList().stream())
                .collect(Collectors.groupingBy(
                        detail -> detail.getProduct().getName(),
                        Collectors.reducing(BigDecimal.ZERO, InvoiceDetail::getTotalPrice, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());

        // Thống kê doanh thu theo thời gian (ví dụ: theo ngày trong tháng hiện tại)
        List<Object[]> revenueByTime = invoices.stream()
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().toLocalDate(),
                        Collectors.summingLong(Invoice::getAmount)))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> new Object[]{entry.getKey().toString(), entry.getValue()})
                .collect(Collectors.toList());

        long totalProductsSold = invoices.stream()
                .flatMap(invoice -> invoice.getInvoiceDetailList().stream())
                .mapToLong(InvoiceDetail::getQuantity)
                .sum();
        List<Product> products = getListProduct(paymentMethod, employeeName, productName);
        SaleReportData reportData = new SaleReportData();
        reportData.setTotalProductsSold(totalProductsSold);
        reportData.setTotalOrders(totalOrders);
        reportData.setTotalRevenue(totalRevenue);
        reportData.setTotalCustomers(totalCustomers);
        reportData.setRevenueByProduct(revenueByProduct);
        reportData.setRevenueByTime(revenueByTime);
        reportData.setFilteredInvoices(invoices);
        reportData.setProducts(products);
        return reportData;
    }

    public List<Object[]> getTotalRevenueAndInvoiceCountByMonthAndYear(List<Invoice> invoices, int month, int year) {
        List<Object[]> dailyStats = new ArrayList<>();

        if (invoices == null || invoices.isEmpty()) {
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                dailyStats.add(new Object[]{day, 0L, 0L});
            }
            return dailyStats;
        }

        // Lọc hóa đơn theo tháng và năm
        List<Invoice> filteredInvoices = invoices.stream()
                .filter(invoice -> invoice.getCreatedAt() != null &&
                        invoice.getCreatedAt().getYear() == year &&
                        invoice.getCreatedAt().getMonthValue() == month)
                .toList();

        // Nhóm hóa đơn theo ngày
        Map<Integer, List<Invoice>> invoicesGroupedByDay = filteredInvoices.stream()
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().getDayOfMonth()));

        // Tính tổng doanh thu mỗi ngày từ chi tiết hóa đơn
        Map<Integer, Long> dailyRevenues = calculateGroupedProfit(invoicesGroupedByDay);

        // Đếm số hóa đơn mỗi ngày
        Map<Integer, Long> dailyCounts = invoicesGroupedByDay.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (long) entry.getValue().size()
                ));

        // Tạo kết quả trả về, bao gồm những ngày không có dữ liệu
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            long revenue = dailyRevenues.getOrDefault(day, 0L);
            long count = dailyCounts.getOrDefault(day, 0L);

            dailyStats.add(new Object[]{day, count, revenue});
        }

        return dailyStats;
    }


    public List<Object[]> getTotalRevenueAndInvoiceCountByMonth(List<Invoice> invoices, int year) {
        List<Object[]> monthlyStats = new ArrayList<>();

        if (invoices == null || invoices.isEmpty()) {

            for (int month = 1; month <= 12; month++) {
                monthlyStats.add(new Object[]{month, 0L, 0L});
            }
            return monthlyStats;
        }

        Map<Integer, List<Invoice>> invoicesGroupedByMonth = invoices.stream()
                .filter(invoice -> invoice.getCreatedAt() != null &&
                        invoice.getCreatedAt().getYear() == year)
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().getMonthValue()));


        Map<Integer, Long> monthlyRevenues = calculateGroupedProfit(invoicesGroupedByMonth);


        Map<Integer, Long> monthlyCounts = invoicesGroupedByMonth.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // key là tháng (Integer)
                        entry -> (long) entry.getValue().size() // value là số lượng hóa đơn (Long)
                ));

        for (int month = 1; month <= 12; month++) {
            long revenue = monthlyRevenues.getOrDefault(month, 0L); // Lấy doanh thu tháng, mặc định 0 nếu không có
            long count = monthlyCounts.getOrDefault(month, 0L); // Lấy số lượng hóa đơn, mặc định 0 nếu không có


            monthlyStats.add(new Object[]{month,count,revenue }); // <-- Tháng là Integer
        }


        return monthlyStats;
    }

    public List<Object[]> getTotalRevenueAndInvoiceCountByYear(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        // Nhóm hóa đơn theo năm
        Map<Integer, List<Invoice>> invoicesGroupedByYear = invoices.stream()
                .filter(invoice -> invoice.getCreatedAt() != null)
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().getYear()));

        // Tính lợi nhuận gộp (doanh thu thực) theo năm
        Map<Integer, Long> revenueByYear = invoicesGroupedByYear.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap(invoice -> invoice.getInvoiceDetailList() != null
                                        ? invoice.getInvoiceDetailList().stream()
                                        : Stream.empty())
                                .filter(detail -> detail.getProduct() != null && detail.getQuantity() != null)
                                .mapToLong(detail -> {
                                    BigDecimal retailPrice = Optional.ofNullable(detail.getProduct().getRetailPrice()).orElse(BigDecimal.ZERO);
                                    BigDecimal purchasePrice = Optional.ofNullable(detail.getProduct().getPurchasePrice()).orElse(BigDecimal.ZERO);
                                    return retailPrice.subtract(purchasePrice)
                                            .multiply(BigDecimal.valueOf(detail.getQuantity()))
                                            .longValue();
                                })
                                .sum()
                ));

        // Đếm số lượng hóa đơn theo năm
        Map<Integer, Long> countByYear = calculateGroupedProfit(invoicesGroupedByYear);
        // Gom dữ liệu ra list kết quả
        List<Object[]> result = new ArrayList<>();
        Set<Integer> years = new TreeSet<>(revenueByYear.keySet()); // Sắp xếp theo năm tăng dần

        for (Integer year : years) {
            long totalRevenue = revenueByYear.getOrDefault(year, 0L);
            long count = countByYear.getOrDefault(year, 0L);
            result.add(new Object[]{year, count, totalRevenue});
        }

        return result;
    }

    public Map<String, Object> generateSalesReport(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            logger.warn("No invoices provided.");
            return null;
        }

        logger.info("Initial invoices count: " + invoices.size());

        long totalOrders = invoices.size();

        long totalRevenue = invoices.stream()
                .flatMap(invoice -> {
                    List<InvoiceDetail> details = invoice.getInvoiceDetailList();
                    return (details == null || details.isEmpty()) ? Stream.empty() : details.stream();
                })
                .filter(detail -> detail.getProduct() != null && detail.getQuantity() != null)
                .mapToLong(detail -> {
                    BigDecimal retailPrice = Optional.ofNullable(detail.getProduct().getRetailPrice()).orElse(BigDecimal.ZERO);
                    BigDecimal purchasePrice = Optional.ofNullable(detail.getProduct().getPurchasePrice()).orElse(BigDecimal.ZERO);
                    return retailPrice.subtract(purchasePrice).multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                })
                .sum();

        long totalProductsSold = invoices.stream()
                .flatMap(invoice -> invoice.getInvoiceDetailList() != null
                        ? invoice.getInvoiceDetailList().stream()
                        : Stream.empty())
                .filter(detail -> detail.getQuantity() != null)
                .mapToLong(InvoiceDetail::getQuantity)
                .sum();

        Map<Integer, Long> revenueByProduct = invoices.stream()
                .flatMap(invoice -> {
                    List<InvoiceDetail> details = invoice.getInvoiceDetailList();
                    return (details == null || details.isEmpty()) ? Stream.empty() : details.stream();
                })
                .filter(detail -> detail.getProduct() != null && detail.getQuantity() != null)
                .collect(Collectors.groupingBy(
                        detail -> detail.getProduct().getProductID(),
                        Collectors.mapping(detail -> {
                            BigDecimal retailPrice = Optional.ofNullable(detail.getProduct().getRetailPrice()).orElse(BigDecimal.ZERO);
                            BigDecimal purchasePrice = Optional.ofNullable(detail.getProduct().getPurchasePrice()).orElse(BigDecimal.ZERO);
                            return retailPrice.subtract(purchasePrice).multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                        }, Collectors.summingLong(Long::longValue))
                ));

        Map<Integer, Product> productDetails = revenueByProduct.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),            // thay cho id -> id
                        productService::findById       // method reference
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("totalOrders", totalOrders);
        report.put("totalRevenue", totalRevenue);
        report.put("totalProductsSold", totalProductsSold);
        report.put("revenueByProduct", revenueByProduct);
        report.put("productDetails", productDetails);

        return report;
    }
    private long calculateProfit(List<Invoice> invoices) {
        return invoices.stream()
                .flatMap(invoice -> invoice.getInvoiceDetailList() != null
                        ? invoice.getInvoiceDetailList().stream()
                        : Stream.empty())
                .filter(detail -> detail.getProduct() != null && detail.getQuantity() != null)
                .mapToLong(detail -> {
                    BigDecimal retailPrice = Optional.ofNullable(detail.getProduct().getRetailPrice()).orElse(BigDecimal.ZERO);
                    BigDecimal purchasePrice = Optional.ofNullable(detail.getProduct().getPurchasePrice()).orElse(BigDecimal.ZERO);
                    return retailPrice.subtract(purchasePrice)
                            .multiply(BigDecimal.valueOf(detail.getQuantity()))
                            .longValue();
                })
                .sum();
    }
    private <K> Map<K, Long> calculateGroupedProfit(Map<K, List<Invoice>> groupedInvoices) {
        return groupedInvoices.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateProfit(entry.getValue())
                ));
    }
}
