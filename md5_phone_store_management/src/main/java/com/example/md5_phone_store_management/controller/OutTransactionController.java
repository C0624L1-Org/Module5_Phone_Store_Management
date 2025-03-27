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
import java.util.Date;
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
            System.out.println("Gỡ lỗi - Giao dịch mới: " + newTransaction);
            System.out.println("Gỡ lỗi - Sản phẩm của giao dịch mới: " + newTransaction.getProduct());
            session.setAttribute("ERROR_MESSAGE", "Sản phẩm mới không hợp lệ.");
            return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
        }

        // Tìm và cập nhật sản phẩm mới
        Product newProduct = productService.getProductById(newTransaction.getProduct().getProductID());
        if (newProduct == null) {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy sản phẩm mới.");
            return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
        }

        // Kiểm tra số lượng tồn kho
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
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
            Employee employee = optionalEmployee.orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy tài khoản"));
            updatedTransaction.setEmployee(employee);
        }

        // Cập nhật giao dịch
        try {
            transactionOutService.updateOutTransaction(Math.toIntExact(oldTransactionID), updatedTransaction);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully");
            session.setAttribute("SUCCESS_MESSAGE", "Cập nhật giao dịch xuất kho " + oldTransactionID + " thành công!");
            return "redirect:/dashboard/admin/transactions/listOut";
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("ERROR_MESSAGE", "Lỗi khi cập nhật giao dịch, vui lòng thử lại!");
            return "redirect:/dashboard/admin/transactions/Out/edit/" + oldTransactionID + "/0";
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

        Product product = productService.getProductById(Math.toIntExact(productId));
        Optional<Product> optionalProduct = Optional.ofNullable(productService.getProductById(Math.toIntExact(productId)));

        if (optionalProduct.isPresent()) {
//            set lại sl
            product.setStockQuantity(product.getStockQuantity() - transaction.getQuantity());
            productService.saveProduct(product);

//            lấy giá và số lượng xuất để lưu tổng
            transaction.setProduct(product);
            transaction.setPurchasePrice(product.getSellingPrice());
            transaction.setTotalPrice(product.getSellingPrice().multiply(BigDecimal.valueOf(transaction.getQuantity())));
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            if (end.before(start)) {
                session.setAttribute("ERROR_MESSAGE", "Ngày kết thúc không thể nhỏ hơn ngày bắt đầu!");
                return "redirect:/dashboard/admin/transactions/listOut";
            }
        }

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

