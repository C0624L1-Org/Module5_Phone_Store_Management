package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.model.dto.SaleReportData;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.implement.CustomerServiceImpl;
import com.example.md5_phone_store_management.service.implement.SaleReportServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    @Autowired
    private IInvoiceService iInvoiceService;

    @Autowired
    private SaleReportServiceImpl saleReportServiceImpl;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/report-home")
    public String showReportHome() {
        return "dashboard/report-management/report-home";
    }

    @GetMapping("/dashboard/admin/customer/report")
    public ModelAndView adminCustomerReport(@RequestParam(name = "page", defaultValue = "0") int page) {
        ModelAndView mv = new ModelAndView("/dashboard/report-management/CustomerReport");
        List<Customer> allCustomers = customerService.findAllCustomers();
        mv.addObject("customers", allCustomers);
        mv.addObject("page", page);
        return mv;
    }

    @GetMapping("/dashboard/admin/customer/report/filler")
    public ModelAndView fillerCustomerReport(@RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(required = false) String gender,
                                             @RequestParam(required = false) Integer age,
                                             @RequestParam(required = false) Integer minPurchaseCount) {
        ModelAndView mv = new ModelAndView("/dashboard/report-management/CustomerReport");
        Gender genderEnum = null;
        if (gender != null && !gender.isBlank()) {
            genderEnum = Arrays.stream(Gender.values())
                    .filter(g -> g.name().equalsIgnoreCase(gender))
                    .findFirst()
                    .orElse(null);
        }
        List<Customer> allCustomers = customerServiceImpl.filterCustomers(genderEnum, age, minPurchaseCount);
        mv.addObject("customers", allCustomers);
        mv.addObject("page", page);
        return mv;
    }

    @GetMapping("/invoice-pdf/{invoiceId}")
    public String generateInvoicePdf(@PathVariable Long invoiceId, Model model) {
        Invoice invoice = iInvoiceService.findById(invoiceId);
        if (invoice == null) {
            return "redirect:/dashboard/sales/form?error=invoice_not_found";
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("transactionId", invoice.getId());
        model.addAttribute("amount", invoice.getAmount());
        model.addAttribute("orderInfo", invoice.getOrderInfo());

        // Sử dụng thông tin từ hóa đơn nếu có, không thì mới dùng giá trị mặc định
        Date paymentDate;
        if (invoice.getPayDate() != null && !invoice.getPayDate().isEmpty()) {
            try {
                paymentDate = new java.text.SimpleDateFormat("yyyyMMddHHmmss").parse(invoice.getPayDate());
            } catch (Exception e) {
                // Nếu có lỗi chuyển đổi, dùng thời gian hiện tại
                paymentDate = new Date();
            }
        } else {
            paymentDate = new Date();
        }
        model.addAttribute("payDate", paymentDate);

        // Thêm phương thức thanh toán vào model
        model.addAttribute("paymentMethod", invoice.getPaymentMethod());

        return "dashboard/sales/payment-invoice";
    }

    @GetMapping("/sales-report")
    public String showSalesReportForm(@RequestParam(value = "paymentMethod", required = false) PaymentMethod paymentMethod,
                                      @RequestParam(value = "employeeName", required = false) String employeeName,
                                      @RequestParam(value = "productName", required = false) String productName,
                                      Model model) {
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("productName", productName);
        return "dashboard/report-management/sales-report";
    }

    @PostMapping("/sales-report")
    public String generateSalesReport(
            @RequestParam(value = "paymentMethod", required = false) PaymentMethod paymentMethod,
            @RequestParam(value = "employeeName", required = false) String employeeName,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(defaultValue = "0") int page, // Thêm tham số page
            Model model) {
        SaleReportData reportData = saleReportServiceImpl.generateSalesReport(paymentMethod, employeeName, productName);
        List<Product> products =reportData.getProducts();
        final int PAGE_SIZE = 3;// 3 sản phẩm mỗi trang
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("productName", productName);
        model.addAttribute("products", products);
        try {

            Map<String, Object> report = saleReportServiceImpl.generateSalesReport(reportData.getFilteredInvoices());
            if (report == null) {
                model.addAttribute("messageType", "error");
                model.addAttribute("message", "Không có dữ liệu trong khoảng thời gian này hoặc mã sản phẩm không hợp lệ.");
                return "dashboard/report-management/sales-report";
            }

            // Nếu có mã sản phẩm, lấy thông tin sản phẩm và gán cho biểu đồ
        /*    if (parsedProductId != null) {
                 productName = getProductNameFromReport(parsedProductId, report);
                if (productName != null) {
                    model.addAttribute("chartProductName", productName);
                    model.addAttribute("chartProductId", parsedProductId);
                }
            }*/

            // Lấy revenueByProduct và phân trang
            Map<Integer, Long> revenueByProduct = (Map<Integer, Long>) report.get("revenueByProduct");
            List<Map.Entry<Integer, Long>> revenueList = revenueByProduct.entrySet().stream()
                    .collect(Collectors.toList());

            // Tính toán phân trang
            int totalItems = revenueList.size();
            int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
            int startIndex = page * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, totalItems);

            // Lấy danh sách sản phẩm cho trang hiện tại
            List<Map.Entry<Integer, Long>> paginatedRevenueList = revenueList.subList(startIndex, endIndex);
            Map<Integer, Long> paginatedRevenueByProduct = paginatedRevenueList.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            model.addAttribute("totalOrders", report.get("totalOrders"));
            model.addAttribute("totalRevenue", report.get("totalRevenue"));
            model.addAttribute("totalProductsSold", report.get("totalProductsSold"));
            model.addAttribute("revenueByProduct", revenueByProduct); // Giữ nguyên để biểu đồ sử dụng
            model.addAttribute("paginatedRevenueByProduct", paginatedRevenueByProduct); // Danh sách phân trang
            model.addAttribute("productDetails", report.get("productDetails"));
            model.addAttribute("showReport", true);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageSize", PAGE_SIZE);

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Mã sản phẩm phải là số nguyên.");
            return "dashboard/report-management/sales-report";
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date: " + e.getMessage());
            model.addAttribute("errorMessage", "Ngày không đúng định dạng (yyyy-MM-dd).");
            return "dashboard/report-management/sales-report";
        }

        return "dashboard/report-management/sales-report";
    }
    // API cho biểu đồ doanh thu theo ngày trong tháng
    @GetMapping("/api/chart/day")
    public ResponseEntity<?> getDailyRevenueChart(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(value = "paymentMethod", required = false) PaymentMethod paymentMethod,
            @RequestParam(value = "employeeName", required = false) String employeeName,
            @RequestParam(value = "productName", required = false) String productName) {


        List<Object[]> data ;
        SaleReportData reportData = saleReportServiceImpl.generateSalesReport(paymentMethod, employeeName, productName);
        List<Invoice> invoices =reportData.getFilteredInvoices();

        if (month != null && year != null) {
            data=saleReportServiceImpl.getTotalRevenueAndInvoiceCountByMonthAndYear(
                    invoices, month,year);
        } else {
            LocalDate now = LocalDate.now();
            int reportMonth = now.getMonthValue(); // tháng hiện tại
            int reportYear = now.getYear();
            data=saleReportServiceImpl.getTotalRevenueAndInvoiceCountByMonthAndYear(
                    invoices, reportYear,reportMonth);
        }

        if (data == null || data.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        Map<String, Object> response = new HashMap<>();
        List<Integer> days = new ArrayList<>();
        List<Long> invoiceCounts = new ArrayList<>();
        List<Long> revenueSums = new ArrayList<>();

        for (Object[] row : data) {
            days.add(((Number) row[0]).intValue());              // Ngày
            invoiceCounts.add(((Number) row[1]).longValue());    // Số hóa đơn
            revenueSums.add(((Number) row[2]).longValue());      // Tổng doanh thu
        }

        response.put("days", days);
        response.put("invoiceCounts", invoiceCounts);
        response.put("revenueSums", revenueSums);


        return ResponseEntity.ok(response);
    }

    // API cho biểu đồ doanh thu theo tháng trong năm
    @GetMapping("/api/revenue/monthly")
    public ResponseEntity<?> getMonthlyRevenueChart(
            @RequestParam(required = false) Integer year,
            @RequestParam(value = "paymentMethod", required = false) PaymentMethod paymentMethod,
            @RequestParam(value = "employeeName", required = false) String employeeName,
            @RequestParam(value = "productName", required = false) String productName) {

        List<Object[]> data;
        SaleReportData reportData = saleReportServiceImpl.generateSalesReport(paymentMethod, employeeName, productName);
        List<Invoice> invoices =reportData.getFilteredInvoices();
        if (year != null) {
           data=saleReportServiceImpl.getTotalRevenueAndInvoiceCountByMonth(
                  invoices, year);
        } else {
            // Lấy dữ liệu mặc định
            int currentYear = Year.now().getValue(); // Lấy năm hiện tại an toàn
            data = saleReportServiceImpl.getTotalRevenueAndInvoiceCountByMonth(
                    invoices, currentYear);
        }

        if (data == null || data.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        Map<String, Object> response = new HashMap<>();
        List<Integer> months = new ArrayList<>();
        List<Long> invoiceCounts = new ArrayList<>();
        List<Long> revenueSums = new ArrayList<>();

        for (Object[] row : data) {
            months.add(((Number) row[0]).intValue());            // Tháng
            invoiceCounts.add(((Number) row[1]).longValue());    // Số hóa đơn
            revenueSums.add(((Number) row[2]).longValue());      // Tổng doanh thu
        }

        response.put("months", months);
        response.put("invoiceCounts", invoiceCounts);
        response.put("revenueSums", revenueSums);

        return ResponseEntity.ok(response);
    }

    // API cho biểu đồ doanh thu theo 3 năm gần nhất
    @GetMapping("/api/revenue/yearly")
    public ResponseEntity<?> getYearlyRevenueChart(
            @RequestParam(value = "paymentMethod", required = false) PaymentMethod paymentMethod,
            @RequestParam(value = "employeeName", required = false) String employeeName,
            @RequestParam(value = "productName", required = false) String productName) {

        List<Object[]> allYearlyData;
        SaleReportData reportData = saleReportServiceImpl.generateSalesReport(paymentMethod, employeeName, productName);
        allYearlyData=saleReportServiceImpl.getTotalRevenueAndInvoiceCountByYear(
                reportData.getFilteredInvoices());

        if (allYearlyData == null || allYearlyData.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        // Sắp xếp theo năm giảm dần và chỉ lấy 3 năm gần nhất
        List<Object[]> data = allYearlyData.stream()
                .sorted((a, b) -> ((Number) b[0]).intValue() - ((Number) a[0]).intValue())
                .limit(3)
                .collect(Collectors.toList());

        // Sắp xếp lại theo năm tăng dần để hiển thị trên biểu đồ
        Collections.sort(data, Comparator.comparingInt(row -> ((Number) row[0]).intValue()));

        Map<String, Object> response = new HashMap<>();
        List<Integer> years = new ArrayList<>();
        List<Long> invoiceCounts = new ArrayList<>();
        List<Long> revenueSums = new ArrayList<>();

        for (Object[] row : data) {
            years.add(((Number) row[0]).intValue());             // Năm
            invoiceCounts.add(((Number) row[1]).longValue());    // Số hóa đơn
            revenueSums.add(((Number) row[2]).longValue());      // Tổng doanh thu
        }

        response.put("years", years);
        response.put("invoiceCounts", invoiceCounts);
        response.put("revenueSums", revenueSums);

        return ResponseEntity.ok(response);
    }
}