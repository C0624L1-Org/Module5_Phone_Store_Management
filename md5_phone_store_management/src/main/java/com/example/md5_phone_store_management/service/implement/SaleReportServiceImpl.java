package com.example.md5_phone_store_management.service.implement;

import com.cloudinary.utils.StringUtils;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.PaymentMethod;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.dto.SaleReportData;
import com.example.md5_phone_store_management.repository.SaleReportRepository;
import com.example.md5_phone_store_management.service.ISaleReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleReportServiceImpl implements ISaleReportService {
    @Autowired
    SaleReportRepository saleReportRepository;

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
    public List<Product> getListProduct(PaymentMethod paymentMethod, String employeeName, String productName){
        return saleReportRepository.findPaidProductsWithFilters(paymentMethod,employeeName,productName);
    }
    public SaleReportData generateSalesReport(PaymentMethod paymentMethod, String employeeName, String productName) {
        if (paymentMethod==null&& StringUtils.isBlank(employeeName)&&StringUtils.isBlank(productName)) {
            List<Invoice> invoices =saleReportRepository.findAll();
        }else {
            List<Invoice> invoices = getFilteredInvoices(paymentMethod, employeeName, productName);
        }
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
    public List<Object[]> getTotalRevenueAndInvoiceCountByMonthAndYear(List<Invoice> invoices, int year, int month) {
        // Tạo danh sách các ngày trong tháng năm đã cho
        List<LocalDate> daysInMonth = getDaysInMonth(year, month);

        // Nhóm các hóa đơn theo ngày trong tháng và năm
        Map<LocalDate, Long> revenueByDay = invoices.stream()
                .filter(invoice -> invoice.getCreatedAt().getYear() == year && invoice.getCreatedAt().getMonthValue() == month)
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().toLocalDate(),
                        Collectors.summingLong(Invoice::getAmount)));

        // Kết quả cuối cùng
        List<Object[]> result = new ArrayList<>();

        // Duyệt qua tất cả các ngày trong tháng
        for (LocalDate date : daysInMonth) {
            long totalRevenue = revenueByDay.getOrDefault(date, 0L); // Lấy doanh thu, nếu không có thì trả về 0
            long count = revenueByDay.containsKey(date) ? 1 : 0; // Nếu có hóa đơn thì có 1 hóa đơn, nếu không có thì 0
            result.add(new Object[]{date, totalRevenue, count});
        }
        return result;
    }

    // Helper method để lấy tất cả các ngày trong tháng và năm
    private List<LocalDate> getDaysInMonth(int year, int month) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        int lengthOfMonth = firstDayOfMonth.lengthOfMonth();  // Lấy số ngày trong tháng
        for (int day = 1; day <= lengthOfMonth; day++) {
            days.add(firstDayOfMonth.withDayOfMonth(day));
        }
        return days;
    }
    public List<Object[]> getTotalRevenueAndInvoiceCountByMonth(List<Invoice> invoices, int year) {
        // Tạo một danh sách các tháng trong năm
        Set<String> allMonthsInYear = getMonthsInYear(year);

        // Nhóm các hóa đơn theo tháng
        Map<String, Long> revenueByMonth = invoices.stream()
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().getYear() + "-" + invoice.getCreatedAt().getMonthValue(),
                        Collectors.summingLong(Invoice::getAmount)));

        // Kết quả cuối cùng
        List<Object[]> result = new ArrayList<>();

        // Duyệt qua tất cả các tháng trong năm
        for (String month : allMonthsInYear) {
            long totalRevenue = revenueByMonth.getOrDefault(month, 0L); // Lấy doanh thu, nếu không có thì trả về 0
            long count = revenueByMonth.containsKey(month) ? 1 : 0; // Nếu có hóa đơn thì có 1 hóa đơn, nếu không có thì 0
            result.add(new Object[]{month, totalRevenue, count});
        }
        return result;
    }

    // Helper method để lấy tất cả các tháng trong năm
    private Set<String> getMonthsInYear(int year) {
        Set<String> months = new HashSet<>();
        for (int i = 1; i <= 12; i++) {
            months.add(year + "-" + i);
        }
        return months;
    }
    public List<Object[]> getTotalRevenueAndInvoiceCountByYear(List<Invoice> invoices) {
        Map<Integer, Long> revenueByYear = invoices.stream()
                .collect(Collectors.groupingBy(invoice -> invoice.getCreatedAt().getYear(),
                        Collectors.summingLong(Invoice::getAmount)));

        List<Object[]> result = new ArrayList<>();


        Set<Integer> years = revenueByYear.keySet();
        for (int year : years) {
            long totalRevenue = revenueByYear.getOrDefault(year, 0L);
            long count = revenueByYear.containsKey(year) ? 1 : 0;
            result.add(new Object[]{year, totalRevenue, count});
        }
        return result;
    }
}

