package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.model.dto.InventoryTransactionDTO;
import com.example.md5_phone_store_management.repository.IInventoryTransactionInRepo;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private IEmployeeService employeeService;

    @Autowired
    private IInventoryTransactionInRepo inventoryTransactionInRepo;

    @GetMapping("")
    public String showImportForm(Model model) {
        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Product> productPage = productService.findAll(pageable);
        List<Product> products = productPage.getContent();
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);
        model.addAttribute("inventoryTransactionDTO", new InventoryTransactionDTO());
        return "dashboard/inventory/import";
    }

    @PostMapping("")
    public String saveImportTransaction(
            @Valid @ModelAttribute("inventoryTransactionDTO") InventoryTransactionDTO inventoryTransactionDTO,
            BindingResult bindingResult,
            Model model) {

        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Product> productPage = productService.findAll(pageable);
        List<Product> products = productPage.getContent();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);

        if (bindingResult.hasErrors()) {
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Dữ liệu nhập không hợp lệ: " + bindingResult.getAllErrors().toString());
            return "dashboard/inventory/import";
        }

        if (inventoryTransactionDTO.getQuantity() <= 0 || inventoryTransactionDTO.getQuantity() > 500) {
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Số lượng phải từ 1 đến 500");
            return "dashboard/inventory/import";
        }

        try {
            Product product = productService.getProductById(inventoryTransactionDTO.getProductID());
            if (product == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại với ID: " + inventoryTransactionDTO.getProductID());
            }

            Supplier supplier = supplierService.getSupplier(inventoryTransactionDTO.getSupplierID());
            if (supplier == null) {
                throw new IllegalArgumentException("Nhà cung cấp không tồn tại với ID: " + inventoryTransactionDTO.getSupplierID());
            }

            Integer employeeId = inventoryTransactionDTO.getEmployeeID();
            if (employeeId == null) {
                throw new IllegalArgumentException("Mã nhân viên không được để trống");
            }
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                throw new IllegalArgumentException("Nhân viên không tồn tại với ID: " + employeeId);
            }

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setSupplier(supplier);
            transaction.setQuantity(inventoryTransactionDTO.getQuantity());
            transaction.setPurchasePrice(inventoryTransactionDTO.getPurchasePrice());
            transaction.setTotalPrice(inventoryTransactionDTO.getPurchasePrice().multiply(BigDecimal.valueOf(inventoryTransactionDTO.getQuantity())));
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setTransactionType(TransactionType.IN);
            transaction.setEmployee(employee);

            inventoryTransactionService.addTransaction(transaction);
            product.setStockQuantity(product.getStockQuantity() + inventoryTransactionDTO.getQuantity());
            productService.save(product);

            return "redirect:/dashboard/stock-in/list?success=true";

        } catch (Exception e) {
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Lỗi khi nhập kho: " + e.getMessage());
            return "dashboard/inventory/import";
        }
    }
}