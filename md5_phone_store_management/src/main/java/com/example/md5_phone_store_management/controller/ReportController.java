package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.SalesReportService;
import com.example.md5_phone_store_management.service.implement.CustomerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private SalesReportService salesReportService;

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    @Autowired
    private IInvoiceService iInvoiceService;

    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private CustomerService customerService;

    @GetMapping("/report-home")
    public String showReportHome(Model model) {
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
    public String showSalesReportForm(Model model) {
        model.addAttribute("startDate", "1970-01-01");
        model.addAttribute("endDate", LocalDate.now().format(DATE_INPUT_FORMATTER));
        int  currentYear = LocalDate.now().getYear();
        model.addAttribute("currentYear", currentYear);
        return "dashboard/report-management/sales-report";
    }

    @PostMapping("/sales-report")
    public String generateSalesReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "0") int page, // Thêm tham số page
            Model model) {

        final int PAGE_SIZE = 3; // 3 sản phẩm mỗi trang

        model.addAttribute("startDate", startDate != null ? startDate.format(DATE_INPUT_FORMATTER) : "");
        model.addAttribute("endDate", endDate != null ? endDate.format(DATE_INPUT_FORMATTER) : "");
        model.addAttribute("productId", productId);

        if (startDate == null || endDate == null) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc.");
            return "dashboard/report-management/sales-report";
        }

        try {
            if (endDate.isBefore(startDate)) {
                model.addAttribute("errorMessage", "Thời gian không hợp lệ: Ngày kết thúc phải sau ngày bắt đầu.");
                return "dashboard/report-management/sales-report";
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            Integer parsedProductId = null;
            if (productId != null && !productId.trim().isEmpty()) {
                parsedProductId = Integer.parseInt(productId);
                if (!salesReportService.isProductIdValid(parsedProductId)) {
                    model.addAttribute("errorMessage", "Mã sản phẩm không tồn tại.");
                    return "dashboard/report-management/sales-report";
                }
            }

            Map<String, Object> report = salesReportService.generateSalesReport(startDateTime, endDateTime, parsedProductId);
            if (report == null) {
                model.addAttribute("errorMessage", "Không có dữ liệu trong khoảng thời gian này hoặc mã sản phẩm không hợp lệ.");
                return "dashboard/report-management/sales-report";
            }

            logger.info("Report Data - Total Orders: " + report.get("totalOrders"));
            logger.info("Report Data - Total Revenue: " + report.get("totalRevenue"));
            logger.info("Report Data - Total Products Sold: " + report.get("totalProductsSold"));
            logger.info("Report Data - Revenue by Product: " + report.get("revenueByProduct"));
            logger.info("Report Data - Product Details: " + report.get("productDetails"));

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
            int  currentYear = LocalDate.now().getYear();
            model.addAttribute("currentYear", currentYear);

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

    @GetMapping("/sales-report-year")
    public String showSalesReportFormYear(Model model,
                                          @RequestParam(name = "year") int year) {

        model.addAttribute("startDate", "1970-01-01");
        model.addAttribute("endDate", LocalDate.now().format(DATE_INPUT_FORMATTER));
        int  currentYear = LocalDate.now().getYear();
        model.addAttribute("currentYear", currentYear);

        // tong so don hang theo nam 1
        int totalInvoiceYear1 = iInvoiceService.getTotalInvoicesYear(year);
        Long totalMoneyInvoiceYear1 = iInvoiceService.getTotalMoneyInvoicesYear(year);

        // tong so don hang theo nam 2
        int totalInvoiceYear2 = iInvoiceService.getTotalInvoicesYear(year-1);
        Long totalMoneyInvoiceYear2 = iInvoiceService.getTotalMoneyInvoicesYear(year-1);

        // tong so don hang theo nam 3
        int totalInvoiceYear3 = iInvoiceService.getTotalInvoicesYear(year-2);
        Long totalMoneyInvoiceYear3 = iInvoiceService.getTotalMoneyInvoicesYear(year-2);

        model.addAttribute("totalInvoiceYear1", totalInvoiceYear1);
        model.addAttribute("totalInvoiceYear2", totalInvoiceYear2);
        model.addAttribute("totalInvoiceYear3", totalInvoiceYear3);
        model.addAttribute("totalMoneyInvoiceYear1", totalMoneyInvoiceYear1);
        model.addAttribute("totalMoneyInvoiceYear2", totalMoneyInvoiceYear2);
        model.addAttribute("totalMoneyInvoiceYear3", totalMoneyInvoiceYear3);
        model.addAttribute("year", year);
        System.out.println(totalMoneyInvoiceYear1);
        System.out.println(totalMoneyInvoiceYear2);
        System.out.println(totalMoneyInvoiceYear3);
        return "dashboard/report-management/sales-report-year";
    }
}