package com.example.md5_phone_store_management.controller;


import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import com.example.md5_phone_store_management.service.ITransactionInService;
import com.example.md5_phone_store_management.service.implement.CustomUserDetailsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class InTransactionController {
    @Autowired
    private ITransactionInService transactionInService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IEmployeeRepository employeeRepository;


    @GetMapping("/admin/transactions/listIn")
    public ModelAndView listInTransactions(@RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        ModelAndView modelAndView = new ModelAndView("/dashboard/transaction/in/list-transaction-in");
        Pageable pageable = PageRequest.of(page, 5);
        Page<InventoryTransaction> transactions = transactionInService.getImportTransactions(pageable);
        modelAndView.addObject("currentPage", page);
        List<Product> productList = productService.findAll(Pageable.unpaged()).getContent();
        modelAndView.addObject("products", productList);
        modelAndView.addObject("suppliers", supplierService.getSupplierList());
        modelAndView.addObject("stockInLists", transactions);
        modelAndView.addObject("totalPage", transactions.getTotalPages());
        return modelAndView;
    }

    @GetMapping("/admin/transactions/listIn/search")
    public ModelAndView searchImportTransactions(
            @RequestParam(name = "productName", required = false) String productName,
            @RequestParam(name = "supplierName", required = false) String supplierName,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        ModelAndView modelAndView = new ModelAndView("/dashboard/transaction/in/list-transaction-in"); // Sửa tên view
        Pageable pageable = PageRequest.of(page, 5);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDate.parse(startDateStr, formatter);
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDate.parse(endDateStr, formatter);
        }

        Page<InventoryTransaction> searchResults = transactionInService.searchImportTransactions(
                productName, supplierName, startDate, endDate, pageable);
        List<Product> productList = productService.findAll(Pageable.unpaged()).getContent();

        // Thêm các thuộc tính vào modelAndView
        modelAndView.addObject("productName", productName);
        modelAndView.addObject("supplierName", supplierName);
        modelAndView.addObject("startDate", startDateStr);
        modelAndView.addObject("endDate", endDateStr);
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPage", searchResults.getTotalPages());
        modelAndView.addObject("stockInLists", searchResults); // Đảm bảo tên biến là "stockInLists"
        modelAndView.addObject("products", productList);
        modelAndView.addObject("suppliers", supplierService.getSupplierList());

        return modelAndView;
    }

    @GetMapping("/admin/transactions/new/in/{id}")
    public String showAddInTransaction(@PathVariable("id") Long id, Model model, HttpSession session) {
        List<Product> products = productService.findAllWithOutPageable();
        List<Supplier> suppliers = supplierService.getSupplierList();

        InventoryTransaction inventoryTransaction = new InventoryTransaction();

        model.addAttribute("inventoryTransaction", inventoryTransaction);
        model.addAttribute("products", products);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("selectedProductId", id);

        if (id == -1) {
            session.setAttribute("ERROR_MESSAGE", "Lỗi: Không thể tải được sản phẩm!");
        } else if (id != 0) {
            Optional<Product> optionalProduct = Optional.ofNullable(productService.getProductById(Math.toIntExact(id)));
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                inventoryTransaction.setProduct(product);
                inventoryTransaction.setSupplier(product.getSupplier());
                model.addAttribute("product", product);
                session.setAttribute("SUCCESS_MESSAGE", "Chọn thành công sản phẩm: " + product.getName() + "!");
            } else {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm này!");
            }
        }

        return "dashboard/transaction/in/create-transaction-in";
    }
    @GetMapping("/admin/transactions/view/{id}")
public ModelAndView showEditInTransaction(@PathVariable("id") Integer id) {
        ModelAndView modelAndView = new ModelAndView("dashboard/transaction/in/view-in");
        modelAndView.addObject("InventoryTransaction", transactionInService.findByInventoryTransactionId(id));
        modelAndView.addObject("products", productService.findAllProducts());
        modelAndView.addObject("suppliers", supplierService.getSupplierList());
        return modelAndView;
    }

    @PostMapping("/admin/transaction/saveNew")
    public String processImport(
            @Valid @ModelAttribute("inventoryTransaction") InventoryTransaction transaction,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            @AuthenticationPrincipal CustomUserDetailsService userDetails) {

        // Validate form input
        if (result.hasErrors()) {
            session.setAttribute("ERROR_MESSAGE", "Dữ liệu nhập không hợp lệ!");
            return "redirect:/dashboard/admin/transactions/new/in/0";
        }

        Long productId = Long.valueOf(transaction.getProduct().getProductID());
        Product product = productService.getProductById(Math.toIntExact(productId));
        Optional<Product> optionalProduct = Optional.ofNullable(product);

        if (optionalProduct.isPresent()) {
            product.setStockQuantity(product.getStockQuantity() + transaction.getQuantity());

            // Sử dụng giá cố định từ sản phẩm (đã là BigDecimal)
            BigDecimal fixedPurchasePrice = product.getPurchasePrice(); // Không cần new BigDecimal
            transaction.setPurchasePrice(fixedPurchasePrice);
            transaction.setTotalPrice(fixedPurchasePrice.multiply(BigDecimal.valueOf(transaction.getQuantity())));
            transaction.setProduct(product);
            transaction.setTransactionType(TransactionType.IN);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setSupplier(product.getSupplier());

            // Get authenticated employee
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
                Employee employee = optionalEmployee.orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
                transaction.setEmployee(employee);
            } else {
                session.setAttribute("ERROR_MESSAGE", "Không thể xác thực người dùng!");
                return "redirect:/dashboard/admin/transactions/new/in/0";
            }

            // Save the transaction
            transactionInService.addInTransaction(transaction);

            session.setAttribute("SUCCESS_MESSAGE", "Lưu lịch sử nhập kho thành công!");
            return "redirect:/dashboard/admin/transactions/listIn";
        } else {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm với ID: " + productId);
            return "redirect:/dashboard/admin/transactions/new/in/0";
        }
    }

    @GetMapping("admin/transactions/In/edit/{inventionId}")
    public String update(Model model,
                         @PathVariable("inventionId") int inventionId) {
        List<Supplier> supplierList = supplierService.getSupplierList();
        InventoryTransaction inventoryTransaction = transactionInService.findById(inventionId);
        Product product = inventoryTransaction.getProduct();
        model.addAttribute("inventoryTransaction", inventoryTransaction);
        model.addAttribute("product", product);
        model.addAttribute("inventionId", inventionId);
        model.addAttribute("supplierList", supplierList);
        return "dashboard/transaction/in/update-transaction-in";
    }





    @PostMapping("/admin/transactions/updateIn")
    public String update(Model model,
                         @RequestParam(name = "inventoryTransactionId") int inventoryTransactionId,
                         @RequestParam(name = "newQuantity") int quantity,
                         @RequestParam(name = "newPurchasePrice") BigDecimal purchasePrice,
                         HttpSession session) {

        try {
            // Get existing transaction
            InventoryTransaction existingTransaction = transactionInService.findById(inventoryTransactionId);
            if (existingTransaction == null) {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy giao dịch!");
                return "redirect:/dashboard/admin/transactions/new/in/0";
            }

            // Get authenticated employee
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                session.setAttribute("ERROR_MESSAGE", "Không thể xác thực người dùng!");
                return "redirect:/dashboard/admin/transactions/new/in/0";
            }

            String username = authentication.getName();
            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
            Employee employee = optionalEmployee.orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

            // Calculate quantity difference and update product inventory
            Product product = existingTransaction.getProduct();
            int oldQuantity = existingTransaction.getQuantity();
            int quantityDifference = quantity - oldQuantity;
            product.setStockQuantity(product.getStockQuantity() - oldQuantity + quantity); // Adjust inventory

            // Create updated transaction
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setQuantity(quantity);
            transaction.setPurchasePrice(purchasePrice);
            transaction.setTotalPrice(purchasePrice.multiply(BigDecimal.valueOf(quantity)));
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setTransactionType(TransactionType.IN);
            transaction.setProduct(product);
            transaction.setEmployee(employee);

            // Save changes within a transaction
            transactionInService.editTransactionById(inventoryTransactionId, transaction);

            session.setAttribute("SUCCESS_MESSAGE", "Lưu lịch sử nhập kho thành công!");
            return "redirect:/dashboard/admin/transactions/listIn";

        } catch (Exception e) {
            session.setAttribute("ERROR_MESSAGE", "Có lỗi xảy ra khi cập nhật giao dịch: " + e.getMessage());
            return "redirect:/dashboard/admin/transactions/new/in/0";
        }
    }
    @PostMapping("/admin/transactions/listIn/delete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteImportTransactions(
            @RequestBody Map<String, List<Integer>> requestBody) {
        Map<String, String> response = new HashMap<>();

        List<Integer> ids = requestBody.get("ids");

        if (ids == null || ids.isEmpty()) {
            response.put("success", "false");
            response.put("message", "Vui lòng chọn ít nhất một giao dịch để xóa.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
           transactionInService.deleteImportTransactions(ids);
            response.put("success", "true");
            response.put("message", "Xóa giao dịch thành công!");
            return ResponseEntity.ok(response);
        } catch (DataAccessException e) {
            response.put("success", "false");
            response.put("message", "Không thể xóa do lỗi dữ liệu.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            response.put("success", "false");
            response.put("message", "Lỗi hệ thống khi xóa giao dịch.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

