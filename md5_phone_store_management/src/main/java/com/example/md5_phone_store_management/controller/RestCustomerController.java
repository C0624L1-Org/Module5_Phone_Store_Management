package com.example.md5_phone_store_management.controller;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.ICustomerRepository;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IProductService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
public class RestCustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired 
    private ICustomerService iCustomerService;
    
    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IProductService iProductService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/dashboard/admin/customers/update")
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @RequestParam Integer customerID,
            @Valid @ModelAttribute Customer customer,
            BindingResult result,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // Nếu có lỗi validation, trả về danh sách lỗi
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            response.put("status", "error");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }


        Customer customerNeedUpdate = customerService.getCustomerByID(customerID);
        BeanUtils.copyProperties(customer, customerNeedUpdate, "customerID");

        boolean isUpdated = customerService.updateCustomer(customerNeedUpdate);
        if (isUpdated) {
            session.setAttribute("SUCCESS_MESSAGE", "Cập Nhật Khách Hàng Thành Công!");
            response.put("status", "success");
            response.put("redirectUrl", "/dashboard/admin/customers/list"); // Điều hướng khi thành công
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Lỗi trong khi cập nhật khách hàng!");
            session.setAttribute("ERROR_MESSAGE", "Lỗi trong khi Cập Nhật Khách Hàng!");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/dashboard/admin/customers/create")
    public ResponseEntity<Map<String, Object>> createCustomer(
            @Valid @ModelAttribute Customer customer,
            BindingResult result,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            response.put("status", "error");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        customerService.addNewCustomerAjax(customer);
        session.setAttribute("SUCCESS_MESSAGE", "Thêm khách hàng thành công!");
        response.put("status", "success");
        response.put("redirectUrl", "/dashboard/admin/customers/list");
        return ResponseEntity.ok(response);
    }

    /** API endpoint để tạo khách hàng mới (Quản lý bán bàng) */
    @PostMapping("/api/create-customer")
    public ResponseEntity<Map<String, Object>> apiCreateCustomer(
            @Valid @ModelAttribute Customer customer,
            BindingResult result,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Log để debug
        System.out.println("API tạo khách hàng mới được gọi với thông tin: " + customer.getFullName() + ", " + customer.getPhone());
        
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            System.out.println("Lỗi validation khi tạo khách hàng: " + errors);
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Kiểm tra phone hoặc email đã tồn tại trước khi thêm khách hàng
            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                // Sử dụng JdbcTemplate để kiểm tra trực tiếp
                String sql = "SELECT COUNT(*) FROM customer WHERE phone = ?";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, customer.getPhone());
                if (count != null && count > 0) {
                    response.put("success", false);
                    response.put("message", "Số điện thoại đã tồn tại trong hệ thống.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                // Sử dụng JdbcTemplate để kiểm tra trực tiếp
                String sql = "SELECT COUNT(*) FROM customer WHERE email = ?";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, customer.getEmail());
                if (count != null && count > 0) {
                    response.put("success", false);
                    response.put("message", "Email đã tồn tại trong hệ thống.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Đảm bảo ngày sinh không null
            if (customer.getDob() == null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    customer.setDob(dateFormat.parse("2000-01-01"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Lưu khách hàng
            Customer savedCustomer = customerService.addNewCustomerAjax(customer);
            
            // Log thông tin khách hàng đã lưu
            System.out.println("Đã thêm khách hàng mới: ID=" + savedCustomer.getCustomerID() + 
                              ", Tên=" + savedCustomer.getFullName() + 
                              ", SĐT=" + savedCustomer.getPhone());
            
            // Chuẩn bị dữ liệu phản hồi
            response.put("success", true);
            response.put("customerId", savedCustomer.getCustomerID());
            response.put("fullName", savedCustomer.getFullName());
            response.put("message", "Thêm khách hàng thành công!");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Xử lý ngoại lệ và ghi log chi tiết
            System.err.println("Lỗi khi thêm khách hàng mới: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Lỗi khi thêm khách hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /** API endpoint tìm kiếm và phân trang cho khách hàng cũ (Quản lý bán hàng) */
    @GetMapping("/api/sales/search-customers")
    public ResponseEntity<Map<String, Object>> searchCustomersWithPurchases(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        System.out.println("API /sales/search-customers được gọi - keyword: " + keyword + ", page: " + page + ", size: " + size);
        
        try {
            PageRequest pageable = PageRequest.of(page, size);
            Page<Customer> customersPage;
            
            if (keyword != null && !keyword.isEmpty()) {
                // Log để debug
                System.out.println("Tìm kiếm khách hàng đã mua với từ khóa: '" + keyword + "'");
                
                // Chuẩn hóa keyword
                String normalizedKeyword = keyword.trim();
                System.out.println("Từ khóa đã chuẩn hóa: '" + normalizedKeyword + "'");
                
                // Kiểm tra từ khóa xem là loại gì
                if (normalizedKeyword.contains("@")) {
                    // Nếu có @ thì đây có thể là email
                    System.out.println("Từ khóa có chứa @, tìm kiếm theo email");
                    // Chỉ tìm kiếm theo email
                    customersPage = iCustomerService.searchCustomerWithPurchases("", "", normalizedKeyword, pageable);
                } else if (normalizedKeyword.matches("\\d+")) {
                    // Nếu chỉ chứa số thì đây là số điện thoại
                    System.out.println("Từ khóa là số, tìm kiếm theo số điện thoại");
                    // Chỉ tìm kiếm theo số điện thoại
                    customersPage = iCustomerService.searchCustomerWithPurchases("", normalizedKeyword, "", pageable);
                } else {
                    // Còn lại là tìm theo tên
                    System.out.println("Từ khóa là chữ, tìm kiếm theo tên");
                    // Chỉ tìm kiếm theo tên
                    customersPage = iCustomerService.searchCustomerWithPurchases(normalizedKeyword, "", "", pageable);
                }
            } else {
                System.out.println("Từ khóa rỗng, hiển thị tất cả khách hàng đã mua");
                customersPage = iCustomerService.findCustomersWithPurchases(pageable);
            }
            
            // Lấy dữ liệu từ Page object
            List<Customer> customers = customersPage.getContent();
            
            // Debug: In ra chi tiết về các khách hàng tìm được
            for (Customer customer : customers) {
                System.out.println("Tìm thấy khách hàng: ID=" + customer.getCustomerID() + 
                                  ", Tên=" + customer.getFullName() + 
                                  ", SĐT=" + customer.getPhone() + 
                                  ", Email=" + customer.getEmail());
            }
            
            long totalElements = customersPage.getTotalElements();
            int totalPages = customersPage.getTotalPages();
            
            System.out.println("Tìm thấy " + customers.size() + " khách hàng đã mua trong trang " + page);
            System.out.println("Tổng số khách hàng đã mua: " + totalElements);
            System.out.println("Tổng số trang: " + totalPages);
            
            // Tạo response map
            Map<String, Object> response = new HashMap<>();
            response.put("content", customers);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("size", size);
            response.put("numberOfElements", customers.size());
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm khách hàng đã mua: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /** API endpoint tìm kiếm và phân trang cho tất cả khách hàng (Quản lý bán hàng) */
    @GetMapping("/api/sales/search-all-customers")
    public ResponseEntity<Map<String, Object>> searchAllCustomers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        System.out.println("API /sales/search-all-customers được gọi - keyword: " + keyword + ", page: " + page + ", size: " + size);
        
        try {
            PageRequest pageable = PageRequest.of(page, size);
            Page<Customer> customersPage;
            
            if (keyword != null && !keyword.isEmpty()) {
                // Log để debug
                System.out.println("Tìm kiếm tất cả khách hàng với từ khóa: '" + keyword + "'");
                
                // Chuẩn hóa keyword
                String normalizedKeyword = keyword.trim();
                System.out.println("Từ khóa đã chuẩn hóa: '" + normalizedKeyword + "'");
                
                // Kiểm tra từ khóa xem là loại gì
                if (normalizedKeyword.contains("@")) {
                    // Nếu có @ thì đây có thể là email
                    System.out.println("Từ khóa có chứa @, tìm kiếm theo email");
                    // Tạo tham số rỗng cho name và phone, chỉ truyền giá trị vào email
                    customersPage = iCustomerService.searchCustomers("", "", normalizedKeyword, pageable);
                } else if (normalizedKeyword.matches("\\d+")) {
                    // Nếu chỉ chứa số thì đây là số điện thoại
                    System.out.println("Từ khóa là số, tìm kiếm theo số điện thoại");
                    customersPage = iCustomerService.searchCustomers("", normalizedKeyword, null, pageable);
                } else {
                    // Còn lại là tìm theo tên
                    System.out.println("Từ khóa là chữ, tìm kiếm theo tên");
                    customersPage = iCustomerService.searchCustomers(normalizedKeyword, "", null, pageable);
                }
            } else {
                System.out.println("Từ khóa rỗng, hiển thị tất cả khách hàng");
                customersPage = iCustomerService.findAllCustomers(pageable);
            }
            
            // Lấy dữ liệu từ Page object
            List<Customer> customers = customersPage.getContent();
            
            // Debug: In ra chi tiết về các khách hàng tìm được
            for (Customer customer : customers) {
                System.out.println("Tìm thấy khách hàng: ID=" + customer.getCustomerID() + 
                                  ", Tên=" + customer.getFullName() + 
                                  ", SĐT=" + customer.getPhone() + 
                                  ", Email=" + customer.getEmail());
            }
            
            long totalElements = customersPage.getTotalElements();
            int totalPages = customersPage.getTotalPages();
            
            System.out.println("Tìm thấy " + customers.size() + " khách hàng trong trang " + page);
            System.out.println("Tổng số khách hàng: " + totalElements);
            System.out.println("Tổng số trang: " + totalPages);
            
            // Tạo response map
            Map<String, Object> response = new HashMap<>();
            response.put("content", customers);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("size", size);
            response.put("numberOfElements", customers.size());
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm tất cả khách hàng: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /** API endpoint tìm kiếm và phân trang cho sản phẩm (Quản lý bán hàng) */
    @GetMapping("/api/sales/search-products")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        PageRequest pageable = PageRequest.of(page, size);
        Page<Product> products;
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (keyword != null && !keyword.isEmpty()) {
                products = iProductService.searchProductsByKeyword(keyword, pageable);
            } else {
                products = iProductService.findAll(pageable);
            }
            
            // Thêm thông tin phân trang vào response
            response.put("content", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("size", products.getSize());
            response.put("numberOfElements", products.getNumberOfElements());
            response.put("number", products.getNumber());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
