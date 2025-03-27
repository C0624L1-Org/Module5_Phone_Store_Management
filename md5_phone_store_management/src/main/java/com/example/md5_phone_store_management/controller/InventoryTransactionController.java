package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
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

        modelAndView.addObject("products", productList);
        modelAndView.addObject("suppliers", supplierService.getSupplierList());
        modelAndView.addObject("currentPage", page);
        modelAndView.addObject("stockInLists", searchResults);
        modelAndView.addObject("totalPage", searchResults.getTotalPages());

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
    @PostMapping("/delete")
    public String deleteImportTransactions(@RequestParam("ids") List<Integer> ids,
                                           Model model,
                                           @ModelAttribute("loggedEmployee") Employee loggedEmployee) {
        if (loggedEmployee == null ||
                (!"ADMIN".equals(loggedEmployee.getRole()) && !"THỦ KHO".equals(loggedEmployee.getRole()))) {
            model.addAttribute("error", "Bạn không có quyền xóa giao dịch!");
            model.addAttribute("messageType", "error");
            return "redirect:/stock-in/list";
        }try{
            inventoryTransactionService.deleteImportTransactions(ids);
            model.addAttribute("message", "Xóa giao dịch thành công!");
            model.addAttribute("messageType", "success");
            return "redirect:/stock-in/list";
        }catch (Exception e) {
            model.addAttribute("message", "Xóa giao dịch thành công!");
            model.addAttribute("messageType", "error");
            return "redirect:/stock-in/list";
        }
    }
}
