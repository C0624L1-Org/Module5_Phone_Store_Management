package com.example.md5_phone_store_management.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = startDate.plusDays(6).atTime(23, 59, 59);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> dailyRevenue = new HashMap<>();
        
        // Tạo danh sách các ngày trong tuần
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            String dateStr = day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dailyRevenue.put(dateStr, 0L); // Giá trị mặc định ban đầu
        }
        
        try {
            // Chuyển định dạng ngày thành yyyyMMddHHmmss
            String start = startDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String end = endDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Integer parsedProductId = null;
            if (productId != null && !productId.isEmpty()) {
                try {
                    parsedProductId = Integer.parseInt(productId);
                } catch (NumberFormatException e) {
                    logger.error("Error parsing productId: " + productId, e);
                    response.put("error", "Mã sản phẩm không hợp lệ");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Lấy tất cả hóa đơn trong khoảng thời gian
            List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(start, end);
            
            for (Invoice invoice : invoices) {
                try {
                    if (invoice.getPayDate() != null) {
                        // Chuyển ngày thanh toán thành đối tượng LocalDate
                        LocalDateTime payDateTime = LocalDateTime.parse(invoice.getPayDate(), 
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                        LocalDate payDate = payDateTime.toLocalDate();
                        
                        // Format lại ngày thanh toán theo định dạng dd/MM/yyyy
                        String dateKey = payDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        
                        if (dailyRevenue.containsKey(dateKey)) {
                            Long currentRevenue = dailyRevenue.get(dateKey);
                            Long revenue = 0L;
                            
                            // Tính doanh thu từ các chi tiết hóa đơn
                            if (invoice.getInvoiceDetailList() != null) {
                                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                    if (parsedProductId != null && 
                                        (detail.getProduct() == null || !detail.getProduct().getProductID().equals(parsedProductId))) {
                                        continue;
                                    }
                                    
                                    if (detail.getProduct() != null && detail.getQuantity() != null) {
                                        BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                            detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                        BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                            detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                        BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                        revenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                    }
                                }
                            }
                            
                            dailyRevenue.put(dateKey, currentRevenue + revenue);
                        }
                    }
                } catch (DateTimeParseException e) {
                    logger.error("Error parsing invoice date: " + invoice.getPayDate(), e);
                }
            }
            
            response.put("labels", dailyRevenue.keySet().stream().sorted().collect(Collectors.toList()));
            response.put("data", dailyRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating weekly revenue data", e);
            response.put("error", "Lỗi khi tạo dữ liệu doanh thu theo tuần");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API cho biểu đồ doanh thu theo tháng (chia thành các tuần)
    @GetMapping("/api/revenue/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String monthYear,
            @RequestParam(required = false) String productId) {
        
        String[] parts = monthYear.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        
        LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
        LocalDateTime endDateTime = lastDayOfMonth.atTime(23, 59, 59);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Chuyển định dạng ngày thành yyyyMMddHHmmss
            String start = startDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String end = endDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Integer parsedProductId = null;
            if (productId != null && !productId.isEmpty()) {
                try {
                    parsedProductId = Integer.parseInt(productId);
                } catch (NumberFormatException e) {
                    logger.error("Error parsing productId: " + productId, e);
                    response.put("error", "Mã sản phẩm không hợp lệ");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Lấy tất cả hóa đơn trong khoảng thời gian
            List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(start, end);
            
            // Chia tháng thành các tuần
            Map<String, Long> weeklyRevenue = new HashMap<>();
            
            // Tìm ngày thứ 2 đầu tiên của tháng
            LocalDate monday = firstDayOfMonth;
            while (monday.getDayOfWeek().getValue() != 1) {
                monday = monday.plusDays(1);
            }
            
            // Tạo các tuần trong tháng
            while (monday.getMonth() == firstDayOfMonth.getMonth() || 
                  (monday.isBefore(lastDayOfMonth) && monday.plusDays(6).isAfter(firstDayOfMonth))) {
                
                LocalDate sunday = monday.plusDays(6);
                String weekLabel = monday.format(DateTimeFormatter.ofPattern("dd/MM")) + 
                                 " - " + 
                                 sunday.format(DateTimeFormatter.ofPattern("dd/MM"));
                
                weeklyRevenue.put(weekLabel, 0L);
                monday = monday.plusDays(7);
            }
            
            // Tính doanh thu theo tuần
            for (Invoice invoice : invoices) {
                try {
                    if (invoice.getPayDate() != null) {
                        // Chuyển ngày thanh toán thành đối tượng LocalDate
                        LocalDateTime payDateTime = LocalDateTime.parse(invoice.getPayDate(), 
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                        LocalDate payDate = payDateTime.toLocalDate();
                        
                        for (String weekLabel : weeklyRevenue.keySet()) {
                            String[] dateParts = weekLabel.split(" - ");
                            String startDateStr = dateParts[0];
                            String endDateStr = dateParts[1];
                            
                            // Tách ngày và tháng
                            String[] startParts = startDateStr.split("/");
                            String[] endParts = endDateStr.split("/");
                            
                            int startDay = Integer.parseInt(startParts[0]);
                            int startMonth = Integer.parseInt(startParts[1]);
                            int endDay = Integer.parseInt(endParts[0]);
                            int endMonth = Integer.parseInt(endParts[1]);
                            
                            // Tạo đối tượng LocalDate cho ngày bắt đầu và kết thúc của tuần
                            LocalDate weekStart = LocalDate.of(year, startMonth, startDay);
                            // Xử lý trường hợp đặc biệt khi tuần kéo dài từ tháng này sang tháng sau
                            LocalDate weekEnd;
                            if (endMonth > startMonth) {
                                weekEnd = LocalDate.of(year, endMonth, endDay);
                            } else {
                                weekEnd = LocalDate.of(year, startMonth, endDay);
                            }
                            
                            if ((payDate.isEqual(weekStart) || payDate.isAfter(weekStart)) && 
                                (payDate.isEqual(weekEnd) || payDate.isBefore(weekEnd))) {
                                
                                Long currentRevenue = weeklyRevenue.get(weekLabel);
                                Long revenue = 0L;
                                
                                // Tính doanh thu từ các chi tiết hóa đơn
                                if (invoice.getInvoiceDetailList() != null) {
                                    for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                                        if (parsedProductId != null && 
                                            (detail.getProduct() == null || !detail.getProduct().getProductID().equals(parsedProductId))) {
                                            continue;
                                        }
                                        
                                        if (detail.getProduct() != null && detail.getQuantity() != null) {
                                            BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                                detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                            BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                                detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                            BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                            revenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                                        }
                                    }
                                }
                                
                                weeklyRevenue.put(weekLabel, currentRevenue + revenue);
                                break;
                            }
                        }
                    }
                } catch (DateTimeParseException e) {
                    logger.error("Error parsing invoice date: " + invoice.getPayDate(), e);
                }
            }
            
            response.put("labels", weeklyRevenue.keySet());
            response.put("data", weeklyRevenue.values());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating monthly revenue data", e);
            response.put("error", "Lỗi khi tạo dữ liệu doanh thu theo tháng");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API cho biểu đồ doanh thu theo năm (theo tháng)
    @GetMapping("/api/revenue/yearly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getYearlyRevenue(
            @RequestParam int year,
            @RequestParam(required = false) String productId) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> monthlyRevenue = new HashMap<>();
        
        // Khởi tạo doanh thu cho 12 tháng
        String[] monthLabels = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                               "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        
        for (String month : monthLabels) {
            monthlyRevenue.put(month, 0L);
        }
        
        try {
            Integer parsedProductId = null;
            if (productId != null && !productId.isEmpty()) {
                try {
                    parsedProductId = Integer.parseInt(productId);
                } catch (NumberFormatException e) {
                    logger.error("Error parsing productId: " + productId, e);
                    response.put("error", "Mã sản phẩm không hợp lệ");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Lặp qua từng tháng trong năm
            for (int month = 1; month <= 12; month++) {
                LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
                LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
                
                LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
                LocalDateTime endDateTime = lastDayOfMonth.atTime(23, 59, 59);
                
                // Chuyển định dạng ngày thành yyyyMMddHHmmss
                String start = startDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String end = endDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                
                // Lấy tất cả hóa đơn trong khoảng thời gian
                List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(start, end);
                
                Long revenue = 0L;
                
                // Tính doanh thu tháng
                for (Invoice invoice : invoices) {
                    if (invoice.getInvoiceDetailList() != null) {
                        for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                            if (parsedProductId != null && 
                                (detail.getProduct() == null || !detail.getProduct().getProductID().equals(parsedProductId))) {
                                continue;
                            }
                            
                            if (detail.getProduct() != null && detail.getQuantity() != null) {
                                BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                    detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                    detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                revenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                            }
                        }
                    }
                }
                
                monthlyRevenue.put(monthLabels[month - 1], revenue);
            }
            
            response.put("labels", Arrays.asList(monthLabels));
            response.put("data", Arrays.stream(monthLabels).map(monthlyRevenue::get).collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating yearly revenue data", e);
            response.put("error", "Lỗi khi tạo dữ liệu doanh thu theo năm");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // API cho biểu đồ doanh thu 3 năm gần đây
    @GetMapping("/api/revenue/multi-year")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMultiYearRevenue(
            @RequestParam(required = false) String productId) {
        
        int currentYear = LocalDate.now().getYear();
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> yearlyRevenue = new HashMap<>();
        
        // Chuyển đổi productId từ String sang Integer nếu có
        Integer parsedProductId = null;
        if (productId != null && !productId.isEmpty()) {
            try {
                parsedProductId = Integer.parseInt(productId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing productId: " + productId, e);
                response.put("error", "Mã sản phẩm không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        // 3 năm gần đây
        for (int i = 2; i >= 0; i--) {
            int year = currentYear - i;
            yearlyRevenue.put("Năm " + year, 0L);
            
            try {
                LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
                LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);
                
                LocalDateTime startDateTime = firstDayOfYear.atStartOfDay();
                LocalDateTime endDateTime = lastDayOfYear.atTime(23, 59, 59);
                
                // Chuyển định dạng ngày thành yyyyMMddHHmmss
                String start = startDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String end = endDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                
                // Lấy tất cả hóa đơn trong khoảng thời gian
                List<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(start, end);
                
                Long revenue = 0L;
                
                // Tính doanh thu năm
                for (Invoice invoice : invoices) {
                    if (invoice.getInvoiceDetailList() != null) {
                        for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                            // Kiểm tra nếu productId được chỉ định thì chỉ tính doanh thu cho sản phẩm đó
                            if (parsedProductId != null && 
                                (detail.getProduct() == null || !detail.getProduct().getProductID().equals(parsedProductId))) {
                                continue;
                            }
                            
                            if (detail.getProduct() != null && detail.getQuantity() != null) {
                                BigDecimal sellingPrice = detail.getProduct().getSellingPrice() != null ? 
                                    detail.getProduct().getSellingPrice() : BigDecimal.ZERO;
                                BigDecimal purchasePrice = detail.getProduct().getPurchasePrice() != null ? 
                                    detail.getProduct().getPurchasePrice() : BigDecimal.ZERO;
                                BigDecimal revenuePerUnit = sellingPrice.subtract(purchasePrice);
                                revenue += revenuePerUnit.multiply(BigDecimal.valueOf(detail.getQuantity())).longValue();
                            }
                        }
                    }
                }
                
                yearlyRevenue.put("Năm " + year, revenue);
            } catch (Exception e) {
                logger.error("Error calculating revenue for year " + year, e);
            }
        }
        
        response.put("labels", yearlyRevenue.keySet().stream().sorted().collect(Collectors.toList()));
        response.put("data", yearlyRevenue.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
}