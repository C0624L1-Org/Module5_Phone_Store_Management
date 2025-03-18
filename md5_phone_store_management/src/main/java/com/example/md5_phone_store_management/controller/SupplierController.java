
package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.SupplierDTO;
import com.example.md5_phone_store_management.service.implement.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("dashboard/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/suppliers")
    public List<Supplier> getAllSuppliers() {
        return supplierService.getSupplierList();
    }

    @GetMapping("create")
    public String createSupplier(Model model) {
        SupplierDTO supplierDTO = new SupplierDTO();
        model.addAttribute("supplierDTO", supplierDTO);
        return "dashboard/supplier/create-supplier";
    }

    @PostMapping("create")
    public String createSupplier(@Valid @ModelAttribute(name="supplierDTO")SupplierDTO supplierDTO,
                                 BindingResult bindingResult,
                                 Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/create-supplier";
        }
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.saveSupplier(supplier);
        return "dashboard/supplier/supplier-home";
    }
    @GetMapping("/update-supplierForm/{id}")
    public String updateSupplier(@PathVariable ("id") Integer id, Model model) {
        Supplier supplier =supplierService.getSupplier(id);
        SupplierDTO supplierDTO = new SupplierDTO();
        BeanUtils.copyProperties(supplier, supplierDTO);
        model.addAttribute("supplierDTO", supplierDTO);
        return "dashboard/supplier/update-supplier-form";
    }
    @PostMapping("/update-supplier")
    public String changeInformSupplier (@Valid @ModelAttribute("supplierDTO") SupplierDTO supplierDTO,
                                        BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/update-supplier-form";
        }
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.updateSupplier(supplier);
        redirectAttributes.addFlashAttribute ("message","updated successfully");
        return "redirect:/dashboard/suppliers";
    }
}

