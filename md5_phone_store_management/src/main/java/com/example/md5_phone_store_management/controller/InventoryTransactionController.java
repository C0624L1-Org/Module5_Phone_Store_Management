package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.repository.IInventoryTransactionInRepo;
import com.example.md5_phone_store_management.service.IInventoryTransactionService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Controller
@RequestMapping("/dashboard")
public class InventoryTransactionController {
    @Autowired
    private IInventoryTransactionService inventoryTransactionService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IInventoryTransactionInRepo inventoryTransactionInRepo;


    @GetMapping("stock-in/list")
    public ModelAndView importController(@RequestParam(name = "page",defaultValue = "0",required = false) int page) {
        ModelAndView modelAndView = new ModelAndView("/dashboard/stock-in/stock-in-list");
      Pageable pageable = PageRequest.of(page,5);
        Page<InventoryTransaction> transactions = inventoryTransactionService.getImportTransactions(pageable);
        modelAndView.addObject("currentPage", page);
        List<Product> productList =productService.findAll(Pageable.unpaged()).getContent();
        modelAndView.addObject("products",productList);
        modelAndView.addObject("suppliers",supplierService.getSupplierList());
      modelAndView.addObject("stockInLists",transactions);
      modelAndView.addObject("totalPage",transactions.getTotalPages());
      return modelAndView;
    }
    @GetMapping("/stock-in/search")
    public ModelAndView searchImportTransactions(
            @RequestParam(name = "productName", required = false) String productName,
            @RequestParam(name = "supplierName", required = false) String supplierName,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        ModelAndView modelAndView = new ModelAndView("/dashboard/stock-in/stock-in-list");
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
        Page<InventoryTransaction> searchResults = inventoryTransactionService.searchImportTransactions(
                productName, supplierName, startDate, endDate, pageable);
        List<Product> productList = productService.findAll(Pageable.unpaged()).getContent();

        modelAndView.addObject("productName", productName);
        modelAndView.addObject("supplierName", supplierName);
        modelAndView.addObject("startDate", startDateStr);
        modelAndView.addObject("endDate", endDateStr);
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("totalPage", searchResults.getTotalPages());
        modelAndView.addObject("stockInLists", searchResults);
        modelAndView.addObject("products", productService.findAll(Pageable.unpaged()).getContent());
        modelAndView.addObject("suppliers", supplierService.getSupplierList());
        return modelAndView;
    }

    @GetMapping("stock-in/update")
    public String update(Model model,
                         @RequestParam(name = "productId") int productId,
                         @RequestParam(name = "supplierId") int supplierId){
        List<Supplier> supplierList = supplierService.getSupplierList();
        InventoryTransaction inventoryTransaction = inventoryTransactionService.getByProductIdAndSupplierId(productId, supplierId);
        model.addAttribute("inventoryTransaction",inventoryTransaction);
        model.addAttribute("supplierList",supplierList);
        return "dashboard/stock-in/stock-in-update";
    }

    @PostMapping("stock-in/update")
    public String update(Model model,
                         @Valid @ModelAttribute("inventoryTransaction") InventoryTransaction inventoryTransaction,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "dashboard/stock-in/stock-in-update";
        }
        InventoryTransaction inventoryTransaction1 = new InventoryTransaction();
        BeanUtils.copyProperties(inventoryTransaction,inventoryTransaction1);
        inventoryTransaction1.setTransactionDate(LocalDateTime.now());
        BigDecimal totalPrice = BigDecimal.valueOf(inventoryTransaction1.getQuantity()) // Chuyển Integer thành BigDecimal
                .multiply(inventoryTransaction1.getPurchasePrice()); // Nhân với BigDecimal

        inventoryTransaction1.setTotalPrice(totalPrice);
        inventoryTransaction1.setTransactionType(TransactionType.IN);
        inventoryTransactionInRepo.save(inventoryTransaction1);
        return "redirect:/dashboard/stock-in/list";
    }
    @PostMapping("/stock-in/delete")
    @PreAuthorize("hasAnyRole('Admin', 'WarehouseStaff')")
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
            inventoryTransactionService.deleteImportTransactions(ids);
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
