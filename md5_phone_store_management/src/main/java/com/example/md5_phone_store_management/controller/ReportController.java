package com.example.md5_phone_store_management.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.IInvoiceRepository;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.SalesReportService;
import com.example.md5_phone_store_management.service.implement.CustomerServiceImpl;

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
    
    @Autowired
    private IInvoiceRepository invoiceRepository;

    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter VNPAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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

    // API cho biểu đồ doanh thu theo tuần
    @GetMapping("/api/revenue/weekly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWeeklyRevenue(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) String productId) {

        try {
            // startDate là ngày đầu của một tuần trong tháng (1, 8, 15, 22, 29)
            YearMonth yearMonth = YearMonth.from(startDate);
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            
            // Tìm tuần chứa startDate (dựa vào ngày trong tháng)
            int dayOfMonth = startDate.getDayOfMonth();
            int weekNumber = (dayOfMonth - 1) / 7; // 0-based: tuần 0, 1, 2, 3, 4
            
            // Tính ngày bắt đầu của tuần đó (ngày 1, 8, 15, 22, 29)
            LocalDate weekStartDate = firstDayOfMonth.plusDays(weekNumber * 7);
            
            // Tính ngày kết thúc của tuần (hoặc ngày cuối tháng nếu tuần bị cắt)
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
            LocalDate weekEndDate = weekStartDate.plusDays(6);
            if (weekEndDate.isAfter(lastDayOfMonth)) {
                weekEndDate = lastDayOfMonth;
            }
            
            Map<String, Object> response = new HashMap<>();
            
            // Danh sách ngày trong tuần (chỉ trong tháng hiện tại)
            List<String> labels = new ArrayList<>();
            List<Long> revenueData = new ArrayList<>();
            List<Integer> productCountData = new ArrayList<>();
            List<BigDecimal> purchasePriceData = new ArrayList<>();
            List<BigDecimal> sellingPriceData = new ArrayList<>();
            Map<String, Object> productInfo = new HashMap<>();
            
            // Format hiển thị ngày Việt Nam: dd/MM
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM");
            
            // Nếu có mã sản phẩm, lấy thông tin sản phẩm
            if (productId != null && !productId.isEmpty()) {
                try {
                    Integer parsedProductId = Integer.parseInt(productId);
                    Product product = salesReportService.findProductById(parsedProductId);
                    if (product != null) {
                        productInfo.put("id", product.getProductID());
                        productInfo.put("name", product.getName());
                        productInfo.put("purchasePrice", product.getPurchasePrice());
                        productInfo.put("sellingPrice", product.getSellingPrice());
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                }
            }
            
            // Tạo dữ liệu cho từng ngày trong tuần (từ weekStartDate đến weekEndDate)
            for (LocalDate currentDate = weekStartDate; !currentDate.isAfter(weekEndDate); currentDate = currentDate.plusDays(1)) {
                String dateLabel = currentDate.format(displayFormatter);
                labels.add(dateLabel);
                
                // Format ngày cho truy vấn database
                String currentDateStr = currentDate.atStartOfDay().format(VNPAY_FORMATTER);
                String nextDateStr = currentDate.plusDays(1).atStartOfDay().format(VNPAY_FORMATTER);
                
                // Lấy doanh thu cho ngày hiện tại
                List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(currentDateStr, nextDateStr);
                
                // Lọc theo mã sản phẩm nếu có
                long dailyRevenue = 0;
                int productsCount = 0;
                BigDecimal purchasePrice = BigDecimal.ZERO;
                BigDecimal sellingPrice = BigDecimal.ZERO;
                
                if (productId != null && !productId.isEmpty()) {
                    // Parse product ID
                    try {
                        Integer parsedProductId = Integer.parseInt(productId);
                        
                        // Lọc và tính doanh thu cho sản phẩm cụ thể
                        for (Invoice invoice : invoices) {
                            if (invoice.getInvoiceDetailList() != null) {
                                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                    if (detail.getProduct() != null && 
                                        detail.getProduct().getProductID().equals(parsedProductId)) {
                                        sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                   detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                        purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                    detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                        BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                        dailyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                        productsCount += detail.getQuantity();
                                    }
                                }
                            }
                        }
                        // Thêm giá nhập và giá bán vào danh sách
                        purchasePriceData.add(purchasePrice);
                        sellingPriceData.add(sellingPrice);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                    }
                } else {
                    // Tính tổng doanh thu cho tất cả sản phẩm
                    for (Invoice invoice : invoices) {
                        if (invoice.getInvoiceDetailList() != null) {
                            for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                if (detail.getProduct() != null && detail.getQuantity() != null) {
                                    BigDecimal currentSellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                         detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                    BigDecimal currentPurchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                          detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                    BigDecimal revenuePerUnit = currentSellingPrice.subtract(currentPurchasePrice);
                                    dailyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                    productsCount += detail.getQuantity();
                                }
                            }
                        }
                    }
                    purchasePriceData.add(BigDecimal.ZERO);
                    sellingPriceData.add(BigDecimal.ZERO);
                }
                
                revenueData.add(dailyRevenue);
                productCountData.add(productsCount);
            }
            
            // Thêm thông tin tuần vào response
            response.put("labels", labels);
            response.put("revenue", revenueData);
            response.put("productCount", productCountData);
            response.put("startDate", weekStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            response.put("endDate", weekEndDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            response.put("weekNumber", weekNumber + 1); // Chuyển từ 0-based sang 1-based
            response.put("month", yearMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("vi", "VN")));
            response.put("year", yearMonth.getYear());
            
            // Thêm thông tin sản phẩm và giá nếu có tìm kiếm theo mã sản phẩm
            if (!productInfo.isEmpty()) {
                response.put("productInfo", productInfo);
                response.put("purchasePriceData", purchasePriceData);
                response.put("sellingPriceData", sellingPriceData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating weekly revenue report: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi khi tạo báo cáo doanh thu theo tuần"));
        }
    }
    
    // API cho biểu đồ doanh thu theo tháng (chia thành các tuần)
    @GetMapping("/api/revenue/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String monthYear,
            @RequestParam(required = false) String productId) {
        
        try {
            // Parse tháng và năm từ tham số
            YearMonth ym = YearMonth.parse(monthYear);

            LocalDate firstDayOfMonth = ym.atDay(1);
            LocalDate lastDayOfMonth = ym.atEndOfMonth();
            
            Map<String, Object> response = new HashMap<>();
            
            // Danh sách các tuần trong tháng (tuần bắt đầu từ ngày 1, mỗi tuần 7 ngày)
            List<String> labels = new ArrayList<>();
            List<Long> revenueData = new ArrayList<>();
            List<Integer> productCountData = new ArrayList<>();
            List<BigDecimal> purchasePriceData = new ArrayList<>();
            List<BigDecimal> sellingPriceData = new ArrayList<>();
            Map<String, Object> productInfo = new HashMap<>();
            
            // Nếu có mã sản phẩm, lấy thông tin sản phẩm
            if (productId != null && !productId.isEmpty()) {
                try {
                    Integer parsedProductId = Integer.parseInt(productId);
                    Product product = salesReportService.findProductById(parsedProductId);
                    if (product != null) {
                        productInfo.put("id", product.getProductID());
                        productInfo.put("name", product.getName());
                        productInfo.put("purchasePrice", product.getPurchasePrice());
                        productInfo.put("sellingPrice", product.getSellingPrice());
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                }
            }
            
            // Đếm số tuần trong tháng
            int numWeeks = (int) Math.ceil((double) lastDayOfMonth.getDayOfMonth() / 7.0);
            
            // Tạo dữ liệu cho từng tuần
            for (int weekNum = 0; weekNum < numWeeks; weekNum++) {
                LocalDate weekStart = firstDayOfMonth.plusDays(weekNum * 7);
                LocalDate weekEnd;
                
                // Nếu là tuần cuối, kết thúc vào ngày cuối tháng
                if (weekNum == numWeeks - 1) {
                    weekEnd = lastDayOfMonth;
                } else {
                    // Kết thúc sau 6 ngày hoặc ngày cuối tháng (nếu tuần bị thiếu ngày)
                    weekEnd = weekStart.plusDays(6);
                    if (weekEnd.isAfter(lastDayOfMonth)) {
                        weekEnd = lastDayOfMonth;
                    }
                }
                
                // Format hiển thị tuần: "DD/MM - DD/MM"
                String weekLabel = weekStart.format(DateTimeFormatter.ofPattern("dd/MM")) + 
                                   " - " + 
                                   weekEnd.format(DateTimeFormatter.ofPattern("dd/MM"));
                labels.add(weekLabel);
                
                // Format ngày cho truy vấn database
                String startDateStr = weekStart.atStartOfDay().format(VNPAY_FORMATTER);
                // Kết thúc là cuối ngày nên thêm 1 ngày rồi lấy lúc 00:00
                String endDateStr = weekEnd.plusDays(1).atStartOfDay().format(VNPAY_FORMATTER);
                
                // Lấy doanh thu cho tuần hiện tại
                List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(startDateStr, endDateStr);
                
                // Lọc theo mã sản phẩm nếu có
                long weeklyRevenue = 0;
                int productsCount = 0;
                BigDecimal purchasePrice = BigDecimal.ZERO;
                BigDecimal sellingPrice = BigDecimal.ZERO;
                
                if (productId != null && !productId.isEmpty()) {
                    // Parse product ID
                    try {
                        Integer parsedProductId = Integer.parseInt(productId);
                        
                        // Lọc và tính doanh thu cho sản phẩm cụ thể
                        for (Invoice invoice : invoices) {
                            if (invoice.getInvoiceDetailList() != null) {
                                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                    if (detail.getProduct() != null && 
                                        detail.getProduct().getProductID().equals(parsedProductId)) {
                                        sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                   detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                        purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                    detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                        BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                        weeklyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                        productsCount += detail.getQuantity();
                                    }
                                }
                            }
                        }
                        // Thêm giá nhập và giá bán vào danh sách
                        purchasePriceData.add(purchasePrice);
                        sellingPriceData.add(sellingPrice);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                    }
                } else {
                    // Tính tổng doanh thu cho tất cả sản phẩm
                    for (Invoice invoice : invoices) {
                        if (invoice.getInvoiceDetailList() != null) {
                            for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                if (detail.getProduct() != null && detail.getQuantity() != null) {
                                    BigDecimal currentSellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                         detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                    BigDecimal currentPurchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                          detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                    BigDecimal revenuePerUnit = currentSellingPrice.subtract(currentPurchasePrice);
                                    weeklyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                    productsCount += detail.getQuantity();
                                }
                            }
                        }
                    }
                    // Thêm giá 0 cho giá nhập và giá bán
                    purchasePriceData.add(BigDecimal.ZERO);
                    sellingPriceData.add(BigDecimal.ZERO);
                }
                
                revenueData.add(weeklyRevenue);
                productCountData.add(productsCount);
            }
            
            response.put("labels", labels);
            response.put("revenue", revenueData);
            response.put("productCount", productCountData);
            response.put("month", ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("vi", "VN")));
            response.put("year", ym.getYear());
            
            // Thêm thông tin sản phẩm và giá nếu có tìm kiếm theo mã sản phẩm
            if (!productInfo.isEmpty()) {
                response.put("productInfo", productInfo);
                response.put("purchasePriceData", purchasePriceData);
                response.put("sellingPriceData", sellingPriceData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating monthly revenue report: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi khi tạo báo cáo doanh thu theo tháng"));
        }
    }
    
    // API cho biểu đồ doanh thu theo năm (theo tháng)
    @GetMapping("/api/revenue/yearly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getYearlyRevenue(
            @RequestParam int year,
            @RequestParam(required = false) String productId) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Danh sách các tháng trong năm
            List<String> labels = Arrays.asList(
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
            );
            
            List<Long> revenueData = new ArrayList<>();
            List<Integer> productCountData = new ArrayList<>();
            List<BigDecimal> purchasePriceData = new ArrayList<>();
            List<BigDecimal> sellingPriceData = new ArrayList<>();
            Map<String, Object> productInfo = new HashMap<>();
            
            // Nếu có mã sản phẩm, lấy thông tin sản phẩm
            if (productId != null && !productId.isEmpty()) {
                try {
                    Integer parsedProductId = Integer.parseInt(productId);
                    Product product = salesReportService.findProductById(parsedProductId);
                    if (product != null) {
                        productInfo.put("id", product.getProductID());
                        productInfo.put("name", product.getName());
                        productInfo.put("purchasePrice", product.getPurchasePrice());
                        productInfo.put("sellingPrice", product.getSellingPrice());
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                }
            }
            
            // Tạo dữ liệu cho từng tháng
            for (int month = 1; month <= 12; month++) {
                YearMonth ym = YearMonth.of(year, month);

                LocalDate firstDayOfMonth = ym.atDay(1);
                LocalDate lastDayOfMonth = ym.atEndOfMonth();
                
                // Format ngày cho truy vấn database
                String startDateStr = firstDayOfMonth.atStartOfDay().format(VNPAY_FORMATTER);
                // Kết thúc là cuối ngày nên thêm 1 ngày rồi lấy lúc 00:00
                String endDateStr = lastDayOfMonth.plusDays(1).atStartOfDay().format(VNPAY_FORMATTER);
                
                // Lấy doanh thu cho tháng hiện tại
                List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(startDateStr, endDateStr);
                
                // Lọc theo mã sản phẩm nếu có
                long monthlyRevenue = 0;
                int productsCount = 0;
                BigDecimal purchasePrice = BigDecimal.ZERO;
                BigDecimal sellingPrice = BigDecimal.ZERO;
                
                if (productId != null && !productId.isEmpty()) {
                    // Parse product ID
                    try {
                        Integer parsedProductId = Integer.parseInt(productId);
                        
                        // Lọc và tính doanh thu cho sản phẩm cụ thể
                        for (Invoice invoice : invoices) {
                            if (invoice.getInvoiceDetailList() != null) {
                                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                    if (detail.getProduct() != null && 
                                        detail.getProduct().getProductID().equals(parsedProductId)) {
                                        sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                   detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                        purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                    detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                        BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                        monthlyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                        productsCount += detail.getQuantity();
                                    }
                                }
                            }
                        }
                        // Thêm giá nhập và giá bán vào danh sách
                        purchasePriceData.add(purchasePrice);
                        sellingPriceData.add(sellingPrice);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                    }
                } else {
                    // Tính tổng doanh thu cho tất cả sản phẩm
                    for (Invoice invoice : invoices) {
                        if (invoice.getInvoiceDetailList() != null) {
                            for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                if (detail.getProduct() != null && detail.getQuantity() != null) {
                                    BigDecimal currentSellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                         detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                    BigDecimal currentPurchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                          detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                    BigDecimal revenuePerUnit = currentSellingPrice.subtract(currentPurchasePrice);
                                    monthlyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                    productsCount += detail.getQuantity();
                                }
                            }
                        }
                    }
                    // Thêm giá 0 cho giá nhập và giá bán
                    purchasePriceData.add(BigDecimal.ZERO);
                    sellingPriceData.add(BigDecimal.ZERO);
                }
                
                revenueData.add(monthlyRevenue);
                productCountData.add(productsCount);
            }
            
            response.put("labels", labels);
            response.put("revenue", revenueData);
            response.put("productCount", productCountData);
            response.put("year", year);
            
            // Thêm thông tin sản phẩm và giá nếu có tìm kiếm theo mã sản phẩm
            if (!productInfo.isEmpty()) {
                response.put("productInfo", productInfo);
                response.put("purchasePriceData", purchasePriceData);
                response.put("sellingPriceData", sellingPriceData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating yearly revenue report: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi khi tạo báo cáo doanh thu theo năm"));
        }
    }
    
    // API cho biểu đồ doanh thu 3 năm gần đây
    @GetMapping("/api/revenue/multi-year")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMultiYearRevenue(
            @RequestParam(required = false) String productId) {

        try {
            Map<String, Object> response = new HashMap<>();
            
            // Lấy năm hiện tại và 2 năm trước đó
            int currentYear = LocalDate.now().getYear();
            
            List<String> labels = new ArrayList<>();
            List<Long> revenueData = new ArrayList<>();
            List<Integer> productCountData = new ArrayList<>();
            List<BigDecimal> purchasePriceData = new ArrayList<>();
            List<BigDecimal> sellingPriceData = new ArrayList<>();
            Map<String, Object> productInfo = new HashMap<>();
            
            // Nếu có mã sản phẩm, lấy thông tin sản phẩm
            if (productId != null && !productId.isEmpty()) {
                try {
                    Integer parsedProductId = Integer.parseInt(productId);
                    Product product = salesReportService.findProductById(parsedProductId);
                    if (product != null) {
                        productInfo.put("id", product.getProductID());
                        productInfo.put("name", product.getName());
                        productInfo.put("purchasePrice", product.getPurchasePrice());
                        productInfo.put("sellingPrice", product.getSellingPrice());
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                }
            }
            
            // Tạo dữ liệu cho 3 năm gần đây
            for (int i = 2; i >= 0; i--) {
                int year = currentYear - i;
                labels.add(String.valueOf(year));
                
                // Tính doanh thu cho cả năm
                LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
                LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);
                
                // Format ngày cho truy vấn database
                String startDateStr = firstDayOfYear.atStartOfDay().format(VNPAY_FORMATTER);
                String endDateStr = lastDayOfYear.plusDays(1).atStartOfDay().format(VNPAY_FORMATTER);
                
                // Lấy doanh thu cho năm hiện tại
                List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(startDateStr, endDateStr);
                
                // Lọc theo mã sản phẩm nếu có
                long yearlyRevenue = 0;
                int productsCount = 0;
                BigDecimal purchasePrice = BigDecimal.ZERO;
                BigDecimal sellingPrice = BigDecimal.ZERO;
                
                if (productId != null && !productId.isEmpty()) {
                    // Parse product ID
                    try {
                        Integer parsedProductId = Integer.parseInt(productId);
                        
                        // Lọc và tính doanh thu cho sản phẩm cụ thể
                        for (Invoice invoice : invoices) {
                            if (invoice.getInvoiceDetailList() != null) {
                                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                    if (detail.getProduct() != null && 
                                        detail.getProduct().getProductID().equals(parsedProductId)) {
                                        sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                   detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                        purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                    detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                        BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                        yearlyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                        productsCount += detail.getQuantity();
                                    }
                                }
                            }
                        }
                        // Thêm giá nhập và giá bán vào danh sách
                        purchasePriceData.add(purchasePrice);
                        sellingPriceData.add(sellingPrice);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Mã sản phẩm không hợp lệ"));
                    }
                } else {
                    // Tính tổng doanh thu cho tất cả sản phẩm
                    for (Invoice invoice : invoices) {
                        if (invoice.getInvoiceDetailList() != null) {
                            for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                if (detail.getProduct() != null && detail.getQuantity() != null) {
                                    BigDecimal currentSellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                         detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                    BigDecimal currentPurchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                          detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                    BigDecimal revenuePerUnit = currentSellingPrice.subtract(currentPurchasePrice);
                                    yearlyRevenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                    productsCount += detail.getQuantity();
                                }
                            }
                        }
                    }
                    // Thêm giá 0 cho giá nhập và giá bán
                    purchasePriceData.add(BigDecimal.ZERO);
                    sellingPriceData.add(BigDecimal.ZERO);
                }
                
                revenueData.add(yearlyRevenue);
                productCountData.add(productsCount);
            }
            
            response.put("labels", labels);
            response.put("revenue", revenueData);
            response.put("productCount", productCountData);
            response.put("currentYear", currentYear);
            
            // Thêm thông tin sản phẩm và giá nếu có tìm kiếm theo mã sản phẩm
            if (!productInfo.isEmpty()) {
                response.put("productInfo", productInfo);
                response.put("purchasePriceData", purchasePriceData);
                response.put("sellingPriceData", sellingPriceData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating multi-year revenue report: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Lỗi khi tạo báo cáo doanh thu nhiều năm"));
        }
    }
}