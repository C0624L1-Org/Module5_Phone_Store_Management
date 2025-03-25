package com.example.md5_phone_store_management.controller;
import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/dashboard/warehouse-staff")
public class InventoryController {

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;

    // Hiển thị giao diện nhập kho
    @GetMapping("/inventory")
    public String showInventoryPage(Model model,
                                    @RequestParam(name="message", required=false) String message,
                                    @RequestParam(name="messageType", required=false) String messageType) {
        List<InventoryTransaction> transactions = inventoryTransactionService.getImportTransactions(Pageable.unpaged());
        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        model.addAttribute("transactions", transactions);
        model.addAttribute("suppliers", suppliers);
        return "dashboard/warehouse-staff/inventory";
    }

    // Lấy danh sách sản phẩm
    @GetMapping("/inventory/products")
    public String listProducts(Model model) {
        List<InventoryTransaction> transactions = inventoryTransactionService.getImportTransactions(Pageable.unpaged());
        model.addAttribute("transactions", transactions);
        return "dashboard/warehouse-staff/fragments/product-modal :: productModalContent";
    }

    // Lấy danh sách nhà cung cấp
    @GetMapping("/inventory/suppliers")
    public String listSuppliers(Model model) {
        List<Supplier> suppliers = inventoryTransactionService.getAllSuppliers();
        model.addAttribute("suppliers", suppliers);
        return "dashboard/warehouse-staff/fragments/supplier-modal :: supplierModalContent";
    }

    // Xử lý quét mã QR
    @GetMapping("/inventory/scan")
    @ResponseBody
    public Object scanQRCode(@RequestParam("code") String code) {
        InventoryTransaction transaction = inventoryTransactionService.findByQRCode(code);
        if (transaction == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm");
        }
        return transaction;
    }

    // **Tích hợp hiển thị QR Code của sản phẩm**
    @GetMapping("/inventory/qrcode/{productId}")
    public ResponseEntity<Resource> getQRCode(@PathVariable Integer productId) throws IOException, WriterException {
        String qrText = "http://localhost:8080/dashboard/warehouse-staff/inventory/" + productId;
        String qrPath = "src/main/resources/static/qrcodes/product_" + productId + ".png";
        QRCodeGenerator.generateQRCode(qrText, qrPath);
        File file = new File(qrPath);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(file));
    }

    // Thêm giao dịch nhập kho
    @PostMapping("/inventory/add")
    public String addInventory(@ModelAttribute InventoryTransaction transaction,
                               RedirectAttributes redirectAttributes) {
        try {
            inventoryTransactionService.addTransaction(transaction);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Nhập kho thành công");
        } catch(Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Thiếu thông tin cần thiết");
        }
        return "redirect:/dashboard/warehouse-staff/inventory";
    }

    // Hủy nhập kho
    @GetMapping("/inventory/cancel")
    public String cancelInventory(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messageType", "info");
        redirectAttributes.addFlashAttribute("message", "Đã hủy nhập kho");
        return "redirect:/dashboard/warehouse-staff/inventory";
    }
}
