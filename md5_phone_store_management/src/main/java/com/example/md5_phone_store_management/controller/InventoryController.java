package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/dashboard/warehouse-staff/inventory")
public class InventoryController {

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;

    @Autowired
    private IProductService productService;

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

    @PostMapping("")
    public String saveImportTransaction(
            @RequestParam("supplierId") Integer supplierId,
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("purchasePrice") String purchasePrice,
            Model model) {

        // Load lại danh sách suppliers và products để hiển thị lại form nếu có lỗi
        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Product> productPage = productService.findAll(pageable);
        List<Product> products = productPage.getContent();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);

        // Kiểm tra thông tin đầu vào
        if (supplierId == null || productId == null || quantity == null || purchasePrice == null || purchasePrice.trim().isEmpty()) {
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Thiếu thông tin cần thiết");
            return "dashboard/inventory/import"; // Quay lại form để nhập lại
        }

        try {
            // Lấy thông tin sản phẩm để hiển thị tên trong thông báo
            Product product = productService.findAll(PageRequest.of(0, 1))
                    .getContent()
                    .stream()
                    .filter(p -> p.getProductID().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại");
            }

            // Thực hiện nhập kho
            inventoryTransactionService.importProduct(productId, quantity, supplierId, purchasePrice);

            // Thành công: Truyền thông báo và redirect về list sau khi đóng modal
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "success");
            model.addAttribute("modalMessage", "Nhập kho " + product.getName() + " thành công");
            model.addAttribute("redirectUrl", "/dashboard/warehouse-staff/inventory/list");
            return "dashboard/inventory/import";
        } catch (Exception e) {
            // Lỗi: Hiển thị thông báo lỗi và ở lại trang nhập kho
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Thiếu thông tin cần thiết hoặc lỗi: " + e.getMessage());
            return "dashboard/inventory/import";
        }
    }

    @GetMapping("/list")
    public String showInventoryList(Model model) {
        List<InventoryTransaction> transactions = inventoryTransactionService.getImportTransactions(null);
        model.addAttribute("transactions", transactions);
        return "dashboard/inventory/list";
    }
}