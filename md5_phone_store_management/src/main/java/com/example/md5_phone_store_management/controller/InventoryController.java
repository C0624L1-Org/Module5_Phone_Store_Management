package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.model.dto.InventoryTransactionDTO;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/dashboard/warehouse-staff/inventory")
public class InventoryController {

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;

    @Autowired
    private IProductRepository productRepository;

    @GetMapping("")
    public String showImportForm(Model model, HttpSession session) {
        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        List<Product> products = productRepository.findAll();
        InventoryTransactionDTO inventoryTransactionDTO = new InventoryTransactionDTO();
        Integer employeeId = (Integer) session.getAttribute("employeeID");
        inventoryTransactionDTO.setEmployeeID(employeeId != null ? employeeId : 1);

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);
        model.addAttribute("inventoryTransactionDTO", inventoryTransactionDTO);

        return "dashboard/inventory/import";
    }

    @PostMapping("")
    public String saveImportTransaction(
            @Valid @ModelAttribute("inventoryTransactionDTO") InventoryTransactionDTO inventoryTransactionDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("suppliers", inventoryTransactionService.getAllSuppliers());
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Dữ liệu nhập không hợp lệ");
            return "dashboard/inventory/import";
        }

        try {
            // Gọi service để lưu và lấy đối tượng giao dịch đã lưu
            InventoryTransaction savedTransaction = inventoryTransactionService.importProduct(
                    inventoryTransactionDTO.getProductID(),
                    inventoryTransactionDTO.getQuantity(),
                    inventoryTransactionDTO.getSupplierID(),
                    inventoryTransactionDTO.getPurchasePrice().toString()
            );

            // Định dạng ngày để hiển thị
            String formattedDate = savedTransaction.getTransactionDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            redirectAttributes.addFlashAttribute("showModal", true);
            redirectAttributes.addFlashAttribute("modalType", "success");
            redirectAttributes.addFlashAttribute("modalMessage",
                    "Nhập kho thành công vào ngày: " + formattedDate);
            return "redirect:/dashboard/stock-in/list";

        } catch (Exception e) {
            model.addAttribute("suppliers", inventoryTransactionService.getAllSuppliers());
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("showModal", true);
            model.addAttribute("modalType", "error");
            model.addAttribute("modalMessage", "Lỗi khi nhập kho: " + e.getMessage());
            return "dashboard/inventory/import";
        }
    }
}