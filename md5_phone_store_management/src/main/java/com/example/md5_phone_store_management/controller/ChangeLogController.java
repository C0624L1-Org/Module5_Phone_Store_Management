package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.repository.InvoiceDetailRepository;
import com.example.md5_phone_store_management.service.*;
import com.example.md5_phone_store_management.service.implement.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import com.example.md5_phone_store_management.service.implement.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/changelogs")
public class ChangeLogController {
    @Autowired
    private InvoiceDetailService invoiceDetailService;

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private ISupplierService iSupplierService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private ICustomerService iCustomerService;

    @Autowired
    private ChangeLogService changeLogService;

    @Autowired
    private IInvoiceService iInvoiceService;

    @Autowired
    private ProductService productService;

    @Autowired
    private TransactionOutService transactionOutService;

    @Autowired
    private TransactionInService transactionInService;


    @Autowired
    private IEmployeeRepository employeeRepository;


//    @GetMapping("/admin-dashboard-chart")
//    public Map<String, Object> getManagerDashboardData() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("products", productService.findAllProducts());
//        response.put("invoiceDetails", invoiceDetailService.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getContent());
//        response.put("customers", iCustomerService.findAllCustomers(PageRequest.of(0, Integer.MAX_VALUE)).getContent());
//        response.put("suppliers", iSupplierService.getSupplierList());
//        response.put("employees", iEmployeeService.getAllEmployeesExceptAdmin(PageRequest.of(0, Integer.MAX_VALUE)).getContent());
//        // Add a timestamp to indicate when data was last updated
//        response.put("lastUpdated", System.currentTimeMillis());
//        // Add aggregated data for charting
//        response.put("totalRevenue", iInvoiceService.totalRevenue());
//        response.put("thisMonthRevenue", iInvoiceService.totalThisMonthInvoiceRevenue());
//        response.put("lastMonthRevenue", iInvoiceService.totalThisMonthInvoiceRevenue());
//        response.put("countAllProducts", productService.countProducts());
//        response.put("countImportProducts", transactionInService.countImportProducts());
//        response.put("countExportProducts", transactionOutService.countExportProducts());
//        response.put("topSellingProduct", invoiceDetailService.getTopSellingProductName());
//        response.put("countSuppliers", iSupplierService.countSuppliers());
//        response.put("newSuppliers", iSupplierService.countSuppliers() - transactionInService.countRegularSupplier());
//        response.put("regularSuppliers", transactionInService.countRegularSupplier());
//        response.put("bestSupplier", transactionInService.getBestSupplierName());
//        response.put("countAllEmployees", iEmployeeService.countEmployee());
//        response.put("countWarehouseStaff", iEmployeeService.countWarehouseStaff());
//        response.put("countSalesStaff", iEmployeeService.countSalesStaff());
//        response.put("countSalesPerson", iEmployeeService.countBusinessStaff());
//        response.put("bestSalesStaff", iEmployeeService.countSalesStaff());
//        response.put("countAllCustomers", iCustomerService.countTotalCustomers());
//        response.put("countNewCustomers", iCustomerService.countNewCustomers());
//        response.put("countRegularCustomers", iCustomerService.countTotalCustomers() - iCustomerService.countNewCustomers());
//        response.put("topBuyingCustomer", invoiceDetailService.getTopBuyingCustomerName());
//        return response;
//    }





    @GetMapping("/sales-staff-home-for-product")
    public Map<String, Object> getProductInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("countProduct", productService.countProducts());
        response.put("bestSaleProduct", invoiceDetailService.getTopSellingProductName());
        return response;
    }


    @GetMapping("/sales-staff-home-info")
    public Map<String, Object> getSaleStaffDashboardData(
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = userDetails.getUsername();
            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
            Employee employee = optionalEmployee.orElseThrow(() ->
                    new RuntimeException("Không tìm thấy tài khoản: " + username));
            Integer employeeID = employee.getEmployeeID();

////          topcard (Doanh Thu Tổng Của Nhân Viên)
            response.put("employeeName", employee.getFullName());
            response.put("employeeRank", iInvoiceService.getEmployeeSellingRank(Long.valueOf(employeeID)));
            response.put("employeeTotalOrdersSold", iInvoiceService.getEmployeeTotalOrdersSold(Long.valueOf(employeeID)));
            response.put("employeeTotalRevenueSold", iInvoiceService.getEmployeeTotalRevenueSold(Long.valueOf(employeeID)));

//            Thống Kê Cá Nhân
            response.put("employeeOrdersToday", iInvoiceService.getEmployeeOrdersToday(Long.valueOf(employeeID)));
            response.put("employeeRevenueToday", iInvoiceService.getEmployeeRevenueToday(Long.valueOf(employeeID)));
            response.put("employeeOrdersThisMonth", iInvoiceService.getEmployeeOrdersThisMonth(Long.valueOf(employeeID)));
            response.put("employeeRevenueThisMonth", iInvoiceService.getEmployeeRevenueThisMonth(Long.valueOf(employeeID)));


        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi khi tải dữ liệu dashboard: " + e.getMessage());
        }
        return response;
    }



    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Map<String, String>> getCustomerById(@PathVariable Long customerId) {
        try {
            Optional<Customer> customerOptional = Optional.ofNullable(iCustomerService.findCustomerById(Math.toIntExact(customerId)));
            if (customerOptional.isPresent()) {
                Customer customer = customerOptional.get();
                Map<String, String> response = new HashMap<>();
                response.put("name", customer.getFullName() != null ? customer.getFullName() : "không xác định");
                return ResponseEntity.ok(response);
            } else {
                // Return 404 with a fallback name to match frontend expectation
                Map<String, String> response = new HashMap<>();
                response.put("name", "không xác định");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error fetching customer with ID " + customerId + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("name", "không xác định");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long productId) {
        try {
            Optional<Product> productOptional = Optional.ofNullable(iProductService.getProductById(Math.toIntExact(productId)));
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("productId", product.getProductID());
                response.put("name", product.getName() != null ? product.getName() : "không xác định");
                return ResponseEntity.ok(response);
            } else {
                // Return 404 with a fallback name to match frontend expectation
                Map<String, Object> response = new HashMap<>();
                response.put("productId", productId);
                response.put("name", "không xác định");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error fetching product with ID " + productId + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("name", "không xác định");
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/lastUpdate/time/transactionin")
    public LocalDateTime getLastTransactionInUpdateTime() {
        return changeLogService.getLastUpdateTimeWithEntityNameAndFieldName("inventorytransaction", "transactionin");

    }

    @GetMapping("/lastUpdate/time/transactionout")
    public LocalDateTime getLastTransactionOutUpdateTime() {
        return changeLogService.getLastUpdateTimeWithEntityNameAndFieldName("inventorytransaction", "transactionout");
    }

    @GetMapping("/lastUpdate/time/suppliers")
    public LocalDateTime getLastSupplierUpdateTime() {
        return changeLogService.getLastUpdateTime("supplier");
    }


    @GetMapping("/warehouse-staff-home/out-of-stock-warning")
    public List<Map<String, Object>> getOutOfStockWarningForWareHouseStaffNotication() {
        List<Product> products = productService.findAllProductsHaveStockQuantityUnderEleven();
        return products.stream().map(product -> {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productID", product.getProductID());
            productMap.put("name", product.getName());
            productMap.put("stockQuantity", product.getStockQuantity());
            productMap.put("generalInfo", String.format(
                    "CPU: %s, Storage: %s, Screen: %s, Camera: %s, Selfie: %s",
                    product.getCPU() != null ? product.getCPU() : "N/A",
                    product.getStorage() != null ? product.getStorage() : "N/A",
                    product.getScreenSize() != null ? product.getScreenSize() : "N/A",
                    product.getCamera() != null ? product.getCamera() : "N/A",
                    product.getSelfie() != null ? product.getSelfie() : "N/A"
            ));
            productMap.put("supplierName", product.getSupplier() != null ? product.getSupplier().getName() : "N/A");
            return productMap;
        }).collect(Collectors.toList());
    }


    @GetMapping("/warehouse-staff-home/notification")
    public List<ChangeLog> getAllChangeLogsForWareHouseStaffNotication() {
        return changeLogService.getAllChangeLogForWarehouse();
    }

//    cập nhạt tg mới nhất chưa đúng cho xuuát nhập

    @GetMapping("/warehouse-staff-dashboard-info")
    public Map<String, Object> getWarehouseStaffDashboardData() {
        Map<String, Object> response = new HashMap<>();

        response.put("allImportQuantity", transactionInService.countImportQuantity());
        response.put("countAllImportProductQuantity", transactionInService.countImportProducts());
        response.put("thisMonthImportQuantity", transactionInService.countThisMonthImportQuantityProducts());
        response.put("recentImportSupplierName", transactionInService.getRecentImportSupplierName());
        response.put("recentImportProductName", transactionInService.getRecentImportProductName());


        response.put("allExportQuantity", transactionOutService.countExportQuantity());
        response.put("countAllExportProductQuantity", transactionOutService.countExportProducts());
        response.put("thisMonthExportQuantity", transactionOutService.countThisMonthExportQuantityProducts());
        response.put("recentExportSupplierName", transactionOutService.getRecentExportSupplierName());
        response.put("recentExportProductName", transactionOutService.getRecentExportProductName());

////        Nhà cung cấp
        response.put("countSuppliers", iSupplierService.countSuppliers());
        response.put("regularSupplier", transactionInService.countRegularSupplier());
        response.put("newSupplier", iSupplierService.countSuppliers() - transactionInService.countRegularSupplier());
        response.put("bestSupplierName", transactionInService.getBestSupplierName());
        response.put("bestSupplierImportQuantity", transactionInService.getBestSupplierImportQuantity());
//
        return response;
    }


    @GetMapping("/admin-dashboard-info")
    public Map<String, Object> getAdminDashboardData() {
        Map<String, Object> response = new HashMap<>();
//        Doanh thu
        response.put("totalRevenue", iInvoiceService.totalRevenue());
        response.put("lastMonthRevenue", iInvoiceService.totalLastMonthInvoiceRevenue());
        response.put("thisMonthRevenue", iInvoiceService.totalThisMonthInvoiceRevenue());

//        Sản phẩm
        response.put("countAllProducts", productService.countProducts());
        response.put("topSellingProductName", invoiceDetailService.getTopSellingProductName());
        response.put("countExportProducts", transactionOutService.countExportProducts());
        response.put("countImportProducts", transactionInService.countImportProducts());
        response.put("topSellingProductNamePurchaseQuantity", invoiceDetailService.getTopSellingProductNamePurchaseQuantity());
//
////        Nhà cung cấp
        response.put("countSuppliers", iSupplierService.countSuppliers());
        response.put("regularSupplier", transactionInService.countRegularSupplier());
        response.put("newSupplier", iSupplierService.countSuppliers() - transactionInService.countRegularSupplier());
        response.put("bestSupplierName", transactionInService.getBestSupplierName());
        response.put("bestSupplierImportQuantity", transactionInService.getBestSupplierImportQuantity());
//
////        nhân viên
        response.put("countAllEmployees", iEmployeeService.countEmployee());
        response.put("countWarehouseStaff", iEmployeeService.countWarehouseStaff());
        response.put("countSalesStaff", iEmployeeService.countSalesStaff());
        response.put("countSalesPerson", iEmployeeService.countBusinessStaff());
        response.put("bestSalesStaffName", iInvoiceService.getBestSalesStaffName());
        response.put("bestSalesStaffSellingQuantity", iInvoiceService.getBestSalesStaffSellingQuantity());
//
////        Khách hàng
        response.put("countAllCustomers", iCustomerService.countTotalCustomers());
        response.put("countNewCustomers", iCustomerService.countNewCustomers());
        response.put("countRegularCustomers", iCustomerService.countTotalCustomers() - iCustomerService.countNewCustomers());
        response.put("topBuyingCustomerName", invoiceDetailService.getTopBuyingCustomerName());
        response.put("topBuyingCustomerTotalPurchase", invoiceDetailService.getTopBuyingCustomerTotalPurchase());
        response.put("topBuyingCustomerTotalPurchaseQuantity", invoiceDetailService.getTopBuyingCustomerTotalPurchaseQuantity());


        return response;
    }


    @GetMapping("/business-home-info")
    public Map<String, Object> getManagementData() {
        Map<String, Object> response = new HashMap<>();
        response.put("countAllProducts", productService.countProducts());
        response.put("countProductsHaveRetailPrice", productService.countProductsHaveRetailPrice());
        response.put("totalCustomers", iCustomerService.countTotalCustomers() != null ? iCustomerService.countTotalCustomers() : 0);
        response.put("maleCustomers", iCustomerService.countMaleCustomers() != null ? iCustomerService.countMaleCustomers() : 0);
        response.put("femaleCustomers", iCustomerService.countFemaleCustomers() != null ? iCustomerService.countFemaleCustomers() : 0);
        response.put("countAllSuccessInvoices", iInvoiceService.countAllSuccessInvoices());
        response.put("countTodaySuccessInvoices", iInvoiceService.countTodaySuccessInvoices());
        response.put("countThisMonthSuccessInvoices", iInvoiceService.countThisMonthSuccessInvoices());
        return response;
    }


    @GetMapping("/report-home-info")
    public Map<String, Object> getReportHome() {
        Map<String, Object> response = new HashMap<>();
        response.put("countAllSuccessInvoices", iInvoiceService.countAllSuccessInvoices());
        response.put("totalInvoiceRevenue", iInvoiceService.totalRevenue());
        response.put("countTodaySuccessInvoices", iInvoiceService.countTodaySuccessInvoices());
        response.put("countThisMonthSuccessInvoices", iInvoiceService.countThisMonthSuccessInvoices());
        response.put("totalTodayInvoiceRevenue", iInvoiceService.totalTodayInvoiceRevenue());
        response.put("totalThisMonthInvoiceRevenue", iInvoiceService.totalThisMonthInvoiceRevenue());
        response.put("totalCustomers", iCustomerService.countTotalCustomers() != null ? iCustomerService.countTotalCustomers() : 0);
        response.put("totalNewCustomers", iCustomerService.countNewCustomers() != null ? iCustomerService.countNewCustomers() : 0);
        return response;
    }


    @GetMapping("/employee/{employeeId}/name")
    public ResponseEntity<Map<String, String>> getEmployeeName(@PathVariable Long employeeId) {
        System.out.println("Received request for employeeId: " + employeeId);
        Map<String, String> response = new HashMap<>();
        try {
            Employee employee = iEmployeeService.getEmployeeById(Math.toIntExact(employeeId));
            if (employee != null) {
                response.put("name", employee.getFullName() != null ? employee.getFullName() : "không xác định");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Nhân viên id " + employeeId + " không tìm thấy.");
                response.put("name", "không xác định");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên nhân viên với ID " + employeeId + ": " + e.getMessage());
            response.put("name", "không xác định");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/invoices/{invoiceId}/details")
    public ResponseEntity<Object> getInvoiceDetails(@PathVariable Long invoiceId) {
        try {
            List<InvoiceDetail> details = invoiceDetailService.findInvoiceDetailById(invoiceId);
            int totalQuantity = details.stream()
                    .mapToInt(detail -> detail.getQuantity() != null ? detail.getQuantity() : 0)
                    .sum();
            System.out.println("Invoice ID " + invoiceId + " has total quantity: " + totalQuantity);
            Object response = new Object() {
                public final Integer quantity = totalQuantity;
            };
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching invoice details for ID " + invoiceId + ": " + e.getMessage());
            e.printStackTrace();
            System.out.println("Error occurred for Invoice ID " + invoiceId + ", returning quantity: 0");
            Object response = new Object() {
                public final Integer quantity = 0;
            };
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/product/notification")
    public ResponseEntity<ChangeLog> getAllProductChangeLogs() {
        ChangeLog changeLog = changeLogService.getLatestEntityChanges("product");
        System.out.println("đã chạy ");
        return ResponseEntity.ok(changeLog != null ? changeLog : new ChangeLog());
    }

    @GetMapping("/invoice/notification")
    public ResponseEntity<ChangeLog> getAllInvoiceChangeLogs() {
        ChangeLog changeLog = changeLogService.getLatestEntityChanges("invoice");
        return ResponseEntity.ok(changeLog != null ? changeLog : new ChangeLog());
    }


    //    thông báo chỉ lấy cái mới nhất thôi tại ít thời gian quá
//     cho kinh doanh
    @GetMapping("/customer/notification")
    public ResponseEntity<ChangeLog> getAllCustomerChangeLogs() {
        ChangeLog changeLog = changeLogService.getLatestEntityChanges("customer");
        return ResponseEntity.ok(changeLog != null ? changeLog : new ChangeLog());
    }


    @GetMapping("/notification")
    public List<ChangeLog> getAllChangeLogs() {
        return changeLogService.getAllChangeLogs();
    }

    //    tất cả
    @GetMapping("/count/all/employees")
    public Long getEmployeeLog() {
        return iEmployeeService.countEmployee();
    }

    @GetMapping("/count/all/customers")
    public Integer countTotalCustomers() {
        return iCustomerService.countTotalCustomers();
    }

    @GetMapping("/count/all/suppliers")
    public Long countSuppliers() {
        return iSupplierService.countSuppliers();
    }

    @GetMapping("/count/all/products")
    public Long countProducts() {
        return iProductService.countProducts();
    }

    @GetMapping("/count/all/total-revenue")
    public ResponseEntity<Double> calculateTotalRevenue() {
        try {
            Long soldProducts = iProductService.countSoldProducts();
            return ResponseEntity.ok(soldProducts != null ? soldProducts.doubleValue() : 0.0);
        } catch (Exception e) {
            System.err.println("Lỗi tính doanh thu: " + e.getMessage());
            return ResponseEntity.ok(0.0);
        }
    }

    //    cập nhật lần cuối
    @GetMapping("/lastUpdate/time/employees")
    public LocalDateTime getLastEmployeeUpdateTime() {
        return changeLogService.getLastUpdateTime("employee");
    }

    @GetMapping("/lastUpdate/time/customers")
    public LocalDateTime getLastCustomerUpdateTime() {
        return changeLogService.getLastUpdateTime("customer");
    }


    @GetMapping("/lastUpdate/time/products")
    public LocalDateTime getLastProductUpdateTime() {
        return changeLogService.getLastUpdateTime("product");
    }

    @GetMapping("/lastUpdate/time/total-revenue")
    public LocalDateTime getLastRevenueUpdateTime() {
        System.out.println("ĐÂY " + changeLogService.getLastUpdateTime("invoice"));
        return changeLogService.getLastUpdateTime("invoice");

    }


    //    đã bán
    @GetMapping("/count/sold/products")
    public Long countSoldProducts() {
        Long soldProducts = iProductService.countSoldProducts();
        return soldProducts != null ? soldProducts : 0L;
    }

}

