package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.InventoryTransaction;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.TransactionType;
import com.example.md5_phone_store_management.repository.IInventoryTransactionInRepo;
import com.example.md5_phone_store_management.repository.ISupplierRepository;
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
    @Autowired
    private ISupplierRepository supplierRepository;

    @GetMapping("stock-in/list")
    public ModelAndView importController(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "success", required = false) Boolean success) {
        ModelAndView modelAndView = new ModelAndView("dashboard/stock-in/stock-in-list");
        Pageable pageable = PageRequest.of(page, 5);
        Page<InventoryTransaction> transactions = inventoryTransactionService.getImportTransactions(pageable);

        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("stockInLists", transactions); // Truyền Page object
        modelAndView.addObject("totalPage", transactions.getTotalPages());
        modelAndView.addObject("products", productService.findAll(Pageable.unpaged()).getContent());
        modelAndView.addObject("suppliers", supplierService.getSupplierList());

        // Thông báo thành công
        if (Boolean.TRUE.equals(success)) {
            modelAndView.addObject("toastType", "success");
            modelAndView.addObject("toastMessage", "Nhập kho thành công!");
        }

        return modelAndView;
    }

//    @GetMapping("/stock-in/search")
//    public ModelAndView searchImportTransactions(
//            @RequestParam(name = "productName", required = false) String productName,
//            @RequestParam(name = "supplierName", required = false) String supplierName,
//            @RequestParam(name = "transactionDate", required = false) String transactionDateStr,
//            @RequestParam(name = "page", defaultValue = "0") int page) {
//        ModelAndView modelAndView = new ModelAndView("/dashboard/stock-in/stock-in-list");
//        Pageable pageable = PageRequest.of(page, 5);
//        LocalDate transactionDate = null;
//        if (transactionDateStr != null && !transactionDateStr.isEmpty()) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Định dạng phù hợp với input date
//            transactionDate = LocalDate.parse(transactionDateStr, formatter);
//        }
//        Page<InventoryTransaction> searchResults = inventoryTransactionService.searchImportTransactions(productName, supplierName, transactionDate, pageable);
//        modelAndView.addObject("currentPage", page);
//        modelAndView.addObject("stockInLists", searchResults);
//        modelAndView.addObject("totalPage", searchResults.getTotalPages());
//        modelAndView.addObject("products", productService.findAll(Pageable.unpaged()).getContent());
//        modelAndView.addObject("suppliers", supplierService.getSupplierList());
//        return modelAndView;
//    }

    @GetMapping("stock-in/update")
    public String update(Model model,
                         @RequestParam(name = "stockInId") int stockInId) {
        List<Supplier> supplierList = supplierService.getSupplierList();
        InventoryTransaction inventoryTransaction = inventoryTransactionService.findById(stockInId);
        model.addAttribute("inventoryTransaction", inventoryTransaction);
        model.addAttribute("supplierList", supplierList);
        return "dashboard/stock-in/stock-in-update";
    }

    @PostMapping("stock-in/update")
    public String update(Model model,
                         @RequestParam(name = "inventoryTransactionId") int inventoryTransactionId,
                         @RequestParam(name = "quantity") int quantity,
                         @RequestParam(name = "purchasePrice") int purchasePrice,
                         @RequestParam(name = "supplier") int supplier
                         ) {
        InventoryTransaction inventoryTransaction =inventoryTransactionService.findById(inventoryTransactionId);
        inventoryTransaction.setQuantity(quantity);
        inventoryTransaction.setTransactionType(TransactionType.IN);
        inventoryTransaction.setTransactionDate(LocalDateTime.now());
        inventoryTransaction.setPurchasePrice(BigDecimal.valueOf(purchasePrice));
        BigDecimal totalPrice = BigDecimal.valueOf(inventoryTransaction.getQuantity())
                .multiply(inventoryTransaction.getPurchasePrice());

        inventoryTransaction.setTotalPrice(totalPrice);
        inventoryTransaction.setSupplier(supplierRepository.findById(supplier).get());
        inventoryTransactionService.addTransaction(inventoryTransaction);

        return "redirect:/dashboard/stock-in/list";

    }
}