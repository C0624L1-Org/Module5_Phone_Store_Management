package com.example.md5_phone_store_management.controller;


import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import com.example.md5_phone_store_management.service.implement.CustomUserDetailsService;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.SupplierService;
import com.example.md5_phone_store_management.service.implement.TransactionOutService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private SupplierService supplierService;



    @GetMapping("/admin/transactions/listOut")
    public ModelAndView listOutTransactions(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        ModelAndView modelAndView = new ModelAndView("dashboard/transaction/out/list-transaction-out");
        Pageable pageable = PageRequest.of(page, 5);
        Page<InventoryTransaction> transactions = transactionOutService.getAllOutTransactionsPage(pageable);
        modelAndView.addObject("inventoryTransactions", transactions);
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPage", transactions.getTotalPages());
        List<Product> productList = productService.findAll(Pageable.unpaged()).getContent();
        modelAndView.addObject("products", productList);
        modelAndView.addObject("suppliers", supplierService.getSupplierList());

        return modelAndView;
    }





    @GetMapping({
            "/admin/transactions/search/out/product",
            "/admin/transactions/search/out/product/{a}",
            "/admin/transactions/search/out/product/{a}/{b}"
    })
    public String searchProducts(
            @PathVariable(value = "a", required = false) String action,
            @PathVariable(value = "b", required = false) String b,
            @RequestParam(value = "nsp", required = false) String productName,
            @RequestParam(value = "nncc", required = false) String supplierName,
            @RequestParam(value = "sl", required = false) String stockSort,
            @RequestParam(value = "g", required = false) String priceSort,
            @RequestParam(value = "ch", required = false) String inStock,
            Model model,
            HttpSession session) {

        // Validate and set default values if parameters are null
        stockSort = stockSort == null ? "" : (stockSort.equals("u") ? "ASC" : (stockSort.equals("d") ? "DESC" : ""));
        priceSort = priceSort == null ? "" : (priceSort.equals("u") ? "ASC" : (priceSort.equals("d") ? "DESC" : ""));
        inStock = inStock == null ? "outStock" : (inStock.equals("1") ? "inStock" : "outStock");

        // Nếu không có action, mặc định là "add"
        if (action == null) {
            action = "add";
        }

        if ("edit".equals(action)) {
            if (b == null || b.isEmpty()) {
                session.setAttribute("ERROR_MESSAGE", "Thiếu ID giao dịch để chỉnh sửa!");
                return "redirect:/dashboard/admin/transactions/listOut";
            }
            model.addAttribute("action", "edit");
            model.addAttribute("supplier", supplierService.getSupplierList());

            InventoryTransaction txn = transactionOutService.getOutTransactionById(Long.parseLong(b))
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            Integer pid = txn.getProduct().getProductID();
            model.addAttribute("pid", pid);
            model.addAttribute("oldId", b);
        } else if ("add".equals(action)) {
            model.addAttribute("action", "add");
            model.addAttribute("supplier", supplierService.getSupplierList());
        } else {
            session.setAttribute("ERROR_MESSAGE", "Lỗi khi tải dữ liệu, vui lòng thao tác lại!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        // Add parameters to model to maintain state
        model.addAttribute("nsp", productName);
        model.addAttribute("nncc", supplierName);
        model.addAttribute("sl", stockSort);
        model.addAttribute("g", priceSort);
        model.addAttribute("ch", inStock);
        model.addAttribute("a", action);
        if (b != null) {
            model.addAttribute("b", b);
        }

        // Fetch product list
        List<Product> listProducts = productService.searchProductToChoose(
                productName, supplierName, stockSort, priceSort, inStock
        );

        if (listProducts.isEmpty()) {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm, vui lòng thêm mới!");
            return "redirect:/dashboard/admin/transactions/listOut";
        }

        model.addAttribute("listProducts", listProducts);
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

    //    dashboard/admin/transactions/search/out/product/{a}/{b}
    @GetMapping("/admin/transactions/choose/out/product/{a}/{b}")
    public String showAddOutTransactionProducts(@PathVariable("a") String action,
                                                @PathVariable(value = "b", required = false) int b,
                                                Model model, HttpSession session) {

        if ("add".equals(action) && (b == 0)) {
            model.addAttribute("supplier", supplierService.getSupplierList());
            model.addAttribute("action", "add");
        } else if ("edit".equals(action)) {
            model.addAttribute("action", "edit");
            model.addAttribute("supplier", supplierService.getSupplierList());
            InventoryTransaction txn = transactionOutService.getOutTransactionById((long) b)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));
            Integer pid = txn.getProduct().getProductID();
            model.addAttribute("pid", pid);
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



    @PostMapping("/admin/transactions/updateOut")
    public String updateTransactionOut(
            @Valid @ModelAttribute("inventoryTransaction") InventoryTransaction newTransaction,
            BindingResult bindingResult,
            @RequestParam("transactionID") Long oldTransactionID,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            @AuthenticationPrincipal CustomUserDetailsService userDetails) {

        // Kiểm tra validation
        if (bindingResult.hasErrors()) {
            session.setAttribute("ERROR_MESSAGE", "Dữ liệu không hợp lệ!");
            return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
        }

        try {
            // Tìm giao dịch cũ
            InventoryTransaction oldInventoryTransaction = transactionOutService
                    .getOutTransactionById(oldTransactionID)
                    .orElse(null);
            if (oldInventoryTransaction == null) {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy giao dịch cũ.");
                return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
            }

            // Kiểm tra và khôi phục số lượng sản phẩm cũ
            Product oldProduct = oldInventoryTransaction.getProduct();
            if (oldProduct == null) {
                session.setAttribute("ERROR_MESSAGE", "Sản phẩm cũ không tồn tại.");
                return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
            }
            oldProduct.setStockQuantity(oldProduct.getStockQuantity() + oldInventoryTransaction.getQuantity());
            productService.saveProduct(oldProduct);

            // Kiểm tra sản phẩm mới
            if (newTransaction.getProduct() == null || newTransaction.getProduct().getProductID() == null) {
                session.setAttribute("ERROR_MESSAGE", "Sản phẩm mới không hợp lệ.");
                return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
            }

            Product newProduct = productService.getProductById(newTransaction.getProduct().getProductID());
            if (newProduct == null) {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm mới.");
                return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
            }

            if (newProduct.getStockQuantity() < newTransaction.getQuantity()) {
                session.setAttribute("ERROR_MESSAGE", "Số lượng xuất vượt quá tồn kho!");
                return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
            }
            newProduct.setStockQuantity(newProduct.getStockQuantity() - newTransaction.getQuantity());
            productService.saveProduct(newProduct);

            // Tạo giao dịch mới
            InventoryTransaction updatedTransaction = new InventoryTransaction();
            updatedTransaction.setQuantity(newTransaction.getQuantity());
            updatedTransaction.setProduct(newProduct);

            BigDecimal Price = newProduct.getSellingPrice() != null ? newProduct.getSellingPrice() : BigDecimal.ZERO;
            updatedTransaction.setPurchasePrice(Price);
            updatedTransaction.setTotalPrice(Price.multiply(BigDecimal.valueOf(newTransaction.getQuantity())));
            updatedTransaction.setSupplier(newTransaction.getSupplier() != null ? newTransaction.getSupplier() : newProduct.getSupplier());
            updatedTransaction.setTransactionType(TransactionType.OUT);
            updatedTransaction.setTransactionDate(LocalDateTime.now());

            // Thiết lập thông tin nhân viên
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                session.setAttribute("ERROR_MESSAGE", "Không thể xác thực người dùng!");
                return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
            }
            String username = authentication.getName();
            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
            Employee employee = optionalEmployee.orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
            updatedTransaction.setEmployee(employee);

            // Cập nhật giao dịch
            transactionOutService.updateOutTransaction(Math.toIntExact(oldTransactionID), updatedTransaction);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully");
            session.setAttribute("SUCCESS_MESSAGE", "Cập nhật giao dịch xuất kho " + oldTransactionID + " thành công!");
            return "redirect:/dashboard/admin/transactions/listOut";

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("ERROR_MESSAGE", "Lỗi khi cập nhật giao dịch: " + e.getMessage());
            return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
        }
    }


    @GetMapping("/admin/transactions/listOut/search")
    public ModelAndView searchTransactionsP(
            @RequestParam(name = "productName", required = false) String productName,
            @RequestParam(name = "supplierName", required = false) String supplierName,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        ModelAndView modelAndView = new ModelAndView("dashboard/transaction/out/list-transaction-out");
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

        Page<InventoryTransaction> searchResults = transactionOutService.searchExportTransactions(
                productName, supplierName, startDate, endDate, pageable);
        List<Product> productList = productService.findAll(Pageable.unpaged()).getContent();

        modelAndView.addObject("productName", productName);
        modelAndView.addObject("supplierName", supplierName);
        modelAndView.addObject("startDate", startDateStr);
        modelAndView.addObject("endDate", endDateStr);
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPage", searchResults.getTotalPages());
        modelAndView.addObject("inventoryTransactions", searchResults); // Sửa ở đây
        modelAndView.addObject("products", productList);
        modelAndView.addObject("suppliers", supplierService.getSupplierList());

        return modelAndView;
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


}

