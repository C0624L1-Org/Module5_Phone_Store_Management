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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
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


    @GetMapping("/admin/transactions/listOut/search")
    public String searchTransactions(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model,
            HttpSession session) throws ParseException {

        List<InventoryTransaction> inventoryTransactions = transactionOutService.searchTransaction(productName, supplierName, startDate, endDate);

        // Check if all parameters are empty or null
        if ((productName == null || productName.isEmpty()) &&
                (supplierName == null || supplierName.isEmpty()) &&
                (startDate == null || startDate.isEmpty()) &&
                (endDate == null || endDate.isEmpty())) {
            session.setAttribute("ERROR_MESSAGE", "Vui lòng nhập trường để tìm kiếm!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        if (inventoryTransactions.isEmpty()) {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy giao dịch phù hợp!");
            return "redirect:/dashboard/admin/transactions/listOut";
        } else {
            if ((productName == null || productName.isEmpty()) &&
                    (supplierName == null || supplierName.isEmpty()) &&
                    (startDate.isEmpty())) {
                session.setAttribute("SUCCESS_MESSAGE", "Bạn đang tìm từ ngày " + new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(endDate)) + " trở về!");
            } else if ((productName == null || productName.isEmpty()) &&
                    (supplierName == null || supplierName.isEmpty()) &&
                    (endDate.isEmpty())) {
                session.setAttribute("SUCCESS_MESSAGE", "Bạn đang tìm từ ngày " + new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(startDate)) + " trở đi!");
            }
        }
        model.addAttribute("inventoryTransactions", inventoryTransactions);
        model.addAttribute("productName", productName);
        model.addAttribute("supplierName", supplierName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "dashboard/transaction/out/list-transaction-out";
    }


    @GetMapping("/admin/transactions/Out/edit/{id}/{pid}")
    public String outTransactionUpdate(@PathVariable("id") Long id,
                                       @PathVariable("pid") Long pid,
                                       Model model,
                                       HttpSession session) {
        // Kiểm tra ID giao dịch
        if (id == null || id == -1) {
            session.setAttribute("ERROR_MESSAGE", "Lỗi! Không tìm thấy giao dịch!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        // Lấy giao dịch
        InventoryTransaction inventoryTransaction = transactionOutService.getOutTransactionById(id).orElse(null);
        if (inventoryTransaction == null) {
            System.out.println("ngu nè " + id);
            session.setAttribute("ERROR_MESSAGE", "Hoá đơn không tồn tại!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        // Lấy sản phẩm cũ từ giao dịch
        Product oldProduct = inventoryTransaction.getProduct();
        if (oldProduct == null) {
            session.setAttribute("ERROR_MESSAGE", "Sản phẩm cũ liên kết với giao dịch không tồn tại!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }
        Product product;
        if (pid == 0) {
            // pid = 0 hiển thị form edit đàua tieu
            product = productService.getProductById(oldProduct.getProductID());
            if (product == null) {
                session.setAttribute("ERROR_MESSAGE", "Sản phẩm cũ không tồn tại trong hệ thống!");
                return "redirect:/dashboard/admin/transactions/listOut";
            }
        } else {
            // Nếu pid != 0, lấy sản phẩm mới
            product = productService.getProductById(Math.toIntExact(pid));
            if (product == null) {
                session.setAttribute("ERROR_MESSAGE", "Sản phẩm mới không tồn tại!");
                return "redirect:/dashboard/admin/transactions/listOut";
            }
            // Cập nhật sản phẩm mới vào giao dịch
            inventoryTransaction.setProduct(product);
        }

        // Thêm dữ liệu vào model
        model.addAttribute("inventoryTransaction", inventoryTransaction);
        model.addAttribute("product", product); // Sản phẩm hiện tại (cũ hoặc mới)
        model.addAttribute("oldProduct", oldProduct); // Sản phẩm cũ ban đầu
        session.setAttribute("SUCCESS_MESSAGE", "Hóa đơn xuất kho: " + id + " !");

        // Trả về trang chỉnh sửa
        return "dashboard/transaction/out/update-transaction-out";
    }


    @GetMapping("/admin/transactions/delete")
    public String deleteCustomers(@RequestParam List<Integer> ids, HttpSession session) {
        for (Integer transactionID : ids) {
            transactionOutService.deleteOutTransaction(transactionID);
        }
        session.setAttribute("SUCCESS_MESSAGE", "Đã Xóa " + ids.size() + " lịch sử giao dịch!");
        return "redirect:/dashboard/admin/transactions/listOut";
    }


    @GetMapping("/admin/transactions/listIn")
    public String listInTransactions(Model model) {
        List<InventoryTransaction> inventoryTransactions = transactionOutService.getAllInTransactions();
        model.addAttribute("inventoryTransactions", inventoryTransactions);
        return "dashboard/transaction/in/list-transaction-in";
    }


    @GetMapping("/admin/transactions/Out/bill/{id}")
    public String outTransactionBill(@PathVariable("id") Long id, Model model, HttpSession session) {
        // Kiểm tra ID hợp lệ
        if (id == -1) {
            session.setAttribute("ERROR_MESSAGE", "Hoá đơn không tồn tại!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        // Lấy Optional từ service và ép về InventoryTransaction
        InventoryTransaction inventoryTransaction = transactionOutService.getOutTransactionById(id).orElse(null);

        // Kiểm tra kết quả
        if (inventoryTransaction != null) {
            model.addAttribute("bill", inventoryTransaction);
            session.setAttribute("SUCCESS_MESSAGE", "Hóa đơn xuất kho: " + id + " !");
            return "dashboard/transaction/out/out-bill";
        } else {
            session.setAttribute("ERROR_MESSAGE", "Hoá đơn không tồn tại!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }
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

        if (id == -1) {
            session.setAttribute("ERROR_MESSAGE", "Lỗi: Không thể tải được sản phẩm!");

        } else if (id != 0) {
            Optional<Product> optionalProduct = Optional.ofNullable(productService.getProductById(Math.toIntExact(id)));

            if (optionalProduct.isPresent()) {
                model.addAttribute("product", optionalProduct.get());

                Product product = optionalProduct.get(); // Lấy đối tượng Product từ Optional
                if (product.getStockQuantity() == 0) {
                    session.setAttribute("ERROR_MESSAGE", "Sản phẩm đã hết hàng! Vui lòng chọn sản phẩm khác!");
                } else {
                    session.setAttribute("SUCCESS_MESSAGE", "Chọn thành công sản phẩm: " + optionalProduct.get().getName() + "!");
                }
            } else {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm này!");
            }
        }
        return "dashboard/transaction/out/create-transaction-out";
    }

    @GetMapping("/admin/transactions/choose/out/product/{a}/{b}")
    public String showAddOutTransactionProducts(@PathVariable("a") String action,
                                                @PathVariable(value = "b", required = false) int b,
                                                Model model, HttpSession session) {
        // Kiểm tra nếu action là "add" và b là null hoặc trống
        if ("add".equals(action) && (b == 0)) {
            model.addAttribute("action", "add");
        } else if ("edit".equals(action)) {
            model.addAttribute("action", "edit");
            model.addAttribute("oldId", b);
        } else {
            session.setAttribute("ERROR_MESSAGE", "Lỗi khi tải dữ liệu, vui lòng thao tác lại!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        // Lấy danh sách sản phẩm
        List<Product> listProducts = productService.findAllWithOutPageable();
        if (listProducts.isEmpty()) {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm, vui lòng thêm mới!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        model.addAttribute("listProducts", listProducts);
        System.out.println("Tổng số sản phẩm: " + listProducts.size());

        return "dashboard/transaction/out/list-products";
    }

    @GetMapping("/warehouse/inventory")
    public String transactionDashboard(Model model) {
        List<InventoryTransaction> transactionIn = transactionOutService.getAllInTransactions();
        List<InventoryTransaction> transactionOut = transactionOutService.getAllOutTransactions();
        model.addAttribute("inTra", transactionIn);
        model.addAttribute("outTra", transactionOut);
        return "dashboard/transaction/in-and-out";
    }


}

