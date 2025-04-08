package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ProductService;
import com.example.md5_phone_store_management.service.SalesReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
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
    private CustomerService customerService;

    @Autowired
    private IProductService iProductService;

    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/report-home")
    public String showReportHome(Model model) {
        return "dashboard/report-management/report-home";
    }

    @GetMapping("/dashboard/admin/customer/report")
    public String adminCustomerReport(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        List<Customer> allCustomers = customerService.findAllCustomers();
        model.addAttribute("customers", allCustomers);
        model.addAttribute("page", page);
        return "dashboard/report-management/CustomerReport";
    }

    @GetMapping("/sales-report")
    public String showSalesReportForm(Model model) {
        model.addAttribute("startDate", "1970-01-01");
        model.addAttribute("endDate", LocalDate.now().format(DATE_INPUT_FORMATTER));
        return "dashboard/report-management/sales-report";
    }

    @RequestMapping(value = "/sales-report", method = {RequestMethod.GET, RequestMethod.POST})
    public String generateSalesReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        model.addAttribute("startDate", startDate != null ? startDate.format(DATE_INPUT_FORMATTER) : "");
        model.addAttribute("endDate", endDate != null ? endDate.format(DATE_INPUT_FORMATTER) : "");
        model.addAttribute("productId", productId);

        if (startDate == null || endDate == null) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc.");
            model.addAttribute("revenueByProduct", Collections.emptyList());
            model.addAttribute("fullRevenueByProduct", Collections.emptyMap());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
            return "dashboard/report-management/sales-report";
        }

        try {
            if (endDate.isBefore(startDate)) {
                model.addAttribute("errorMessage", "Thời gian không hợp lệ: Ngày kết thúc phải sau ngày bắt đầu.");
                model.addAttribute("revenueByProduct", Collections.emptyList());
                model.addAttribute("fullRevenueByProduct", Collections.emptyMap());
                model.addAttribute("totalPages", 0);
                model.addAttribute("currentPage", 0);
                return "dashboard/report-management/sales-report";
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            Integer parsedProductId = null;
            if (productId != null && !productId.trim().isEmpty()) {
                parsedProductId = Integer.parseInt(productId);
                if (!salesReportService.isProductIdValid(parsedProductId)) {
                    model.addAttribute("errorMessage", "Mã sản phẩm không tồn tại.");
                    model.addAttribute("revenueByProduct", Collections.emptyList());
                    model.addAttribute("fullRevenueByProduct", Collections.emptyMap());
                    model.addAttribute("totalPages", 0);
                    model.addAttribute("currentPage", 0);
                    return "dashboard/report-management/sales-report";
                }
            }

            Map<String, Object> report = salesReportService.generateSalesReport(startDateTime, endDateTime, parsedProductId);
            if (report == null || report.get("revenueByProduct") == null) {
                model.addAttribute("errorMessage", "Không có dữ liệu trong khoảng thời gian này hoặc mã sản phẩm không hợp lệ.");
                model.addAttribute("revenueByProduct", Collections.emptyList());
                model.addAttribute("fullRevenueByProduct", Collections.emptyMap());
                model.addAttribute("totalPages", 0);
                model.addAttribute("currentPage", 0);
                return "dashboard/report-management/sales-report";
            }

            @SuppressWarnings("unchecked")
            Map<String, Double> revenueByProduct = (Map<String, Double>) report.get("revenueByProduct");

            // Phân trang
            int pageSize = 3;
            int totalItems = revenueByProduct.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            page = Math.max(0, Math.min(page, totalPages - 1));

            List<Map.Entry<String, Double>> productList = revenueByProduct.entrySet().stream()
                    .skip(page * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            // Lấy thông tin sản phẩm cho tooltip
            Map<String, Product> productDetails = new HashMap<>();
            for (Map.Entry<String, Double> entry : productList) {
                Integer prodId = Integer.parseInt(entry.getKey());
                Product product = productService.findById(prodId);
                if (product != null) {
                    productDetails.put(entry.getKey(), product);
                }
            }

            // Truyền dữ liệu
            model.addAttribute("totalOrders", report.get("totalOrders"));
            model.addAttribute("totalRevenue", report.get("totalRevenue"));
            model.addAttribute("totalProductsSold", report.get("totalProductsSold"));
            model.addAttribute("revenueByProduct", productList); // Danh sách phân trang
            model.addAttribute("fullRevenueByProduct", revenueByProduct); // Dữ liệu đầy đủ cho biểu đồ
            model.addAttribute("productDetails", productDetails); // Thông tin sản phẩm
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("showReport", true);

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Mã sản phẩm phải là số nguyên.");
            model.addAttribute("revenueByProduct", Collections.emptyList());
            model.addAttribute("fullRevenueByProduct", Collections.emptyMap());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
            return "dashboard/report-management/sales-report";
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date: " + e.getMessage());
            model.addAttribute("errorMessage", "Ngày không đúng định dạng (yyyy-MM-dd).");
            model.addAttribute("revenueByProduct", Collections.emptyList());
            model.addAttribute("fullRevenueByProduct", Collections.emptyMap());
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 0);
            return "dashboard/report-management/sales-report";
        }

        return "dashboard/report-management/sales-report";
    }
}