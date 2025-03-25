package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IInventoryTransactionInRepo;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/dashboard/warehouse-staff/inventory")
public class InventoryController {

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;

    @Autowired
    private IProductService productService;

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private IInventoryTransactionInRepo inventoryTransactionInRepo;

    // Hiển thị form nhập kho
    @GetMapping("")
    public String showImportForm(Model model) {
        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Product> productPage = productService.findAll(pageable);
        List<Product> products = productPage.getContent();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);
        return "dashboard/inventory/import";
    }

    // Xử lý nhập kho với modal thông báo
    @PostMapping("")
    public String saveImportTransaction(
            @RequestParam("supplierId") Integer supplierId,
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("purchasePrice") String purchasePrice,
            Model model) {

        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Product> productPage = productService.findAll(pageable);
        List<Product> products = productPage.getContent();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);

        if (supplierId == null || productId == null || quantity == null || purchasePrice == null || purchasePrice.trim().isEmpty()) {
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Thiếu thông tin cần thiết");
            return "dashboard/inventory/import";
        }

        try {
            Product product = productService.findAll(PageRequest.of(0, 1))
                    .getContent()
                    .stream()
                    .filter(p -> p.getProductID().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại");
            }

            inventoryTransactionService.importProduct(productId, quantity, supplierId, purchasePrice);

            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "success");
            model.addAttribute("modalMessage", "Nhập kho " + product.getName() + " thành công");
            model.addAttribute("redirectUrl", "/dashboard/warehouse-staff/inventory/list");
            return "dashboard/inventory/import";
        } catch (Exception e) {
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Thiếu thông tin cần thiết hoặc lỗi: " + e.getMessage());
            return "dashboard/inventory/import";
        }
    }

    // Hiển thị danh sách giao dịch nhập kho
    @GetMapping("/list")
    public ModelAndView showInventoryList(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        ModelAndView modelAndView = new ModelAndView("dashboard/inventory/list");
        Pageable pageable = PageRequest.of(page, 5);
        Page<InventoryTransaction> transactions = inventoryTransactionService.getImportTransactions(pageable);

        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("transactions", transactions.getContent());
        modelAndView.addObject("totalPage", transactions.getTotalPages());
        return modelAndView;
    }

    // Tìm kiếm giao dịch nhập kho
    @GetMapping("/search")
    public ModelAndView searchImportTransactions(
            @RequestParam(name = "productName", required = false) String productName,
            @RequestParam(name = "supplierName", required = false) String supplierName,
            @RequestParam(name = "transactionDate", required = false) String transactionDateStr,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        ModelAndView modelAndView = new ModelAndView("dashboard/inventory/list");
        Pageable pageable = PageRequest.of(page, 5);
        LocalDate transactionDate = null;
        if (transactionDateStr != null && !transactionDateStr.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            transactionDate = LocalDate.parse(transactionDateStr, formatter);
        }

        Page<InventoryTransaction> searchResults = inventoryTransactionService.searchImportTransactions(productName, supplierName, transactionDate, pageable);
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("transactions", searchResults.getContent());
        modelAndView.addObject("totalPage", searchResults.getTotalPages());
        return modelAndView;
    }

    // Hiển thị form cập nhật giao dịch
    @GetMapping("/update")
    public String showUpdateForm(Model model,
                                 @RequestParam(name = "productId") int productId,
                                 @RequestParam(name = "supplierId") int supplierId) {
        List<Supplier> supplierList = supplierService.getSupplierList();
        InventoryTransaction inventoryTransaction = inventoryTransactionService.getByProductIdAndSupplierId(productId, supplierId);
        model.addAttribute("inventoryTransaction", inventoryTransaction);
        model.addAttribute("supplierList", supplierList);
        return "dashboard/stock-in/stock-in-update";
    }

    // Xử lý cập nhật giao dịch
    @PostMapping("/update")
    public String updateTransaction(
            @Valid @ModelAttribute("inventoryTransaction") InventoryTransaction inventoryTransaction,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierList", supplierService.getSupplierList());
            return "dashboard/stock-in/stock-in-update";
        }

        InventoryTransaction inventoryTransaction1 = new InventoryTransaction();
        BeanUtils.copyProperties(inventoryTransaction, inventoryTransaction1);
        inventoryTransaction1.setTransactionDate(LocalDateTime.now());
        BigDecimal totalPrice = BigDecimal.valueOf(inventoryTransaction1.getQuantity())
                .multiply(inventoryTransaction1.getPurchasePrice());
        inventoryTransaction1.setTotalPrice(totalPrice);
        inventoryTransaction1.setTransactionType(TransactionType.IN);
        inventoryTransactionInRepo.save(inventoryTransaction1);
        return "redirect:/dashboard/warehouse-staff/inventory/list";
    }
}