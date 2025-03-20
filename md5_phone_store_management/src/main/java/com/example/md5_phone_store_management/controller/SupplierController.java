package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Supplier;

import com.example.md5_phone_store_management.service.implement.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dashboard/warehouse/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public String listSuppliers(Model model) {
        List<Supplier> suppliers = supplierService.getSupplierList();
        model.addAttribute("suppliers", suppliers);
        return "dashboard/supplier/supplier-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "dashboard/supplier/supplier-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Supplier supplier = supplierService.getSupplier(id);
        model.addAttribute("supplier", supplier);
        return "dashboard/supplier/supplier-form";
    }

    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute Supplier supplier) {
        supplierService.saveSupplier(supplier);
        return "redirect:/dashboard/warehouse/supplier";
    }


//    khoa test delete
//    @GetMapping("/delete/{id}")
//    public String deleteSupplier(@PathVariable("id") Integer id) {
//        supplierService.deleteSupplier(id);
//        return "redirect:/dashboard/warehouse/supplier";
//    }

    @GetMapping("/search")
    public String searchSuppliers(@RequestParam("keyword") String keyword, Model model) {
        List<Supplier> suppliers = supplierService.searchSuppliers(keyword);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("keyword", keyword);
        return "supplier-list";
    }


}
