package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.service.*;
import com.example.md5_phone_store_management.service.implement.ChangeLogService;
import com.example.md5_phone_store_management.service.implement.CustomerServiceImpl;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.SupplierService;
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
    private ICustomerService iCustomerService;


    @Autowired
    private ChangeLogService changeLogService;


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

        model.addAttribute("countAllSuccessInvoices", iInvoiceService.countAllSuccessInvoices());
        model.addAttribute("totalInvoiceRevenue", iInvoiceService.totalRevenue());

        model.addAttribute("countTodaySuccessInvoices", iInvoiceService.countTodaySuccessInvoices());
        model.addAttribute("countThisMonthSuccessInvoices", iInvoiceService.countThisMonthSuccessInvoices());

        System.out.println("Doanh thu tháng này " + iInvoiceService.totalThisMonthInvoiceRevenue() + "\n" +" Doanh thu hôm nay " + iInvoiceService.totalTodayInvoiceRevenue()+ " \n " + "Tổng doanh thu " + iInvoiceService.totalRevenue());




        model.addAttribute("totalTodayInvoiceRevenue", iInvoiceService.totalTodayInvoiceRevenue());
        model.addAttribute("totalThisMonthInvoiceRevenue", iInvoiceService.totalThisMonthInvoiceRevenue());

        model.addAttribute("totalCustomers", iCustomerService.countTotalCustomers() != null ? iCustomerService.countTotalCustomers() : 0);
        model.addAttribute("totalNewCustomers", iCustomerService.countNewCustomers() != null ? iCustomerService.countNewCustomers() : 0);
        //        khách hàng thân thiết thì trừ đi xử lý ở fe

//thông báo gọi api sau
        return "dashboard/report-management/report-home";
    }
    //        thông báo cho khách hàng là khách hàng nào đã thanh toán thành công(invoice)
//        báo cáo bán hàng là đơn hàng mới nhất(invoice)

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
}