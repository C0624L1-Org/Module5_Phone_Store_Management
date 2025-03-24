package com.example.md5_phone_store_management.controller;


import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.implement.CustomUserDetailsService;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.TransactionOutService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class OutTransactionController {

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private TransactionOutService transactionOutService;




    @GetMapping("/admin/transactions/listOut")
    public String listOutTransactions(Model model) {
        List<InventoryTransaction> inventoryTransactions = transactionOutService.getAllOutTransactions();
        model.addAttribute("inventoryTransactions", inventoryTransactions);
        return "dashboard/transaction/out/list-transaction-out";
    }

    @GetMapping("/admin/transactions/listIn")
    public String listInTransactions(Model model) {
        List<InventoryTransaction> inventoryTransactions = transactionOutService.getAllInTransactions();
        model.addAttribute("inventoryTransactions", inventoryTransactions);
        return "dashboard/transaction/in/list-transaction-in";
    }









    @PostMapping("/admin/transactions/saveNew")
    public String processExport(@Valid @ModelAttribute("inventoryTransaction") InventoryTransaction transaction,
                                BindingResult result,
                                @RequestParam("productId") Long productId,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                HttpSession session,
                                @AuthenticationPrincipal CustomUserDetailsService userDetails) {
//        CustomUserDetails
        Product product = productService.getProductById(Math.toIntExact(productId));
        Optional<Product> optionalProduct = Optional.ofNullable(productService.getProductById(Math.toIntExact(productId)));

        if (optionalProduct.isPresent()) {
//            set lại sl
            product.setStockQuantity(product.getStockQuantity() - transaction.getQuantity());
            productService.saveProduct(product);

//            lấy giá và số lượng xuất để lưu tổng
            transaction.setProduct(product);
            transaction.setPurchasePrice(product.getPurchasePrice());
            transaction.setTotalPrice(product.getPurchasePrice().multiply(BigDecimal.valueOf(transaction.getQuantity())));
            transaction.setTransactionType(TransactionType.OUT);
            transaction.setTransactionDate(LocalDateTime.now()); // Thời gian hiện tại
            transaction.setSupplier(product.getSupplier()); // Lấy từ product hoặc form
            // Lấy thông tin tài khoản đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
                Employee employee = optionalEmployee.orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy tài khoản"));
                transaction.setEmployee(employee);
            }

            transactionOutService.addOutTransaction(transaction);
            session.setAttribute("SUCCESS_MESSAGE", "Lưu lịch sử xuất kho thành công!");
            return "redirect:/dashboard/admin/transactions/listOut";
        } else {
            session.setAttribute("ERROR_MESSAGE", "Lỗi không thể lưu!");
            return "redirect:/dashboard/admin/transactions/new/out/0";
        }

    }





    @GetMapping("/admin/transactions/new/out/{id}")
    public String showAddOutTransaction(@PathVariable("id") Long id, Model model, HttpSession session) {
        // Luôn thêm một đối tượng InventoryTransaction mới vào model
        model.addAttribute("inventoryTransaction", new InventoryTransaction());

        if (id != 0) {
            Optional<Product> optionalProduct = Optional.ofNullable(productService.getProductById(Math.toIntExact(id)));

            if (optionalProduct.isPresent()) {
                model.addAttribute("product", optionalProduct.get());
                session.setAttribute("SUCCESS_MESSAGE", "Chọn thành công sản phẩm: " + optionalProduct.get().getName() + "!");
            } else {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm này!");
            }
        }

        return "dashboard/transaction/out/create-transaction-out";
    }


    @GetMapping("/admin/transactions/new/out/product")
    public String showAddOutTransactionProducts(Model model) {
        List<Product> listProducts = productService.findAllWithOutPageable();
        model.addAttribute("listProducts", listProducts);
        System.out.println("tổng " + listProducts.size());
        return "dashboard/transaction/out/list-products";
    }


    @GetMapping("/warehouse/inventory")
    public String transactionDashboard(Model model) {
        return "dashboard/transaction/in-and-out";
    }


}

