package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.SupplierDTO;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.implement.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private IEmployeeService iEmployeeService;

    // Hiển thị danh sách nhà cung cấp với phân trang và tìm kiếm
    @GetMapping("/suppliers")
    public String getAllSuppliers(
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "address", required = false, defaultValue = "") String address,
            @RequestParam(value = "phone", required = false, defaultValue = "") String phone,
            @RequestParam(value = "email", required = false, defaultValue = "") String email,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        Page<Supplier> suppliers = supplierService.searchSuppliers(
                name.trim().isEmpty() ? null : name.trim(),
                address.trim().isEmpty() ? null : address.trim(),
                phone.trim().isEmpty() ? null : phone.trim(),
                email.trim().isEmpty() ? null : email.trim(),
                page, size);

        model.addAttribute("suppliers", suppliers.getContent());
        model.addAttribute("totalPages", suppliers.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("name", name);
        model.addAttribute("address", address);
        model.addAttribute("phone", phone);
        model.addAttribute("email", email);

        return "dashboard/supplier/supplier-list";
    }

    // Tạo nhà cung cấp mới - GET
    @GetMapping("/suppliers/create")
    public String createSupplier(Model model) {
        model.addAttribute("supplierDTO", new SupplierDTO());
        return "dashboard/supplier/create-supplier";
    }

    // Tạo nhà cung cấp mới - POST
    @PostMapping("/suppliers/create")
    public String createSupplier(
            @Valid @ModelAttribute("supplierDTO") SupplierDTO supplierDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (supplierService.existsByEmail(supplierDTO.getEmail()) || iEmployeeService.existsByEmail(supplierDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.supplier", "Email đã tồn tại!");
        }

        supplierDTO.validate(supplierDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/create-supplier";
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.saveSupplier(supplier);

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Tạo nhà cung cấp thành công!");
        return "redirect:/dashboard/suppliers";
    }

    // Cập nhật nhà cung cấp - GET
    @GetMapping("/update-supplierForm/{id}")
    public String updateSupplier(@PathVariable("id") Integer id, Model model) {
        Supplier supplier = supplierService.getSupplier(id);
        if (supplier == null) {
            return "redirect:/dashboard/suppliers";
        }
        SupplierDTO supplierDTO = new SupplierDTO();
        BeanUtils.copyProperties(supplier, supplierDTO);
        model.addAttribute("supplierDTO", supplierDTO);
        return "dashboard/supplier/update-supplier-form";
    }

    // Cập nhật nhà cung cấp - POST
    @PostMapping("/update-supplier")
    public String changeInformSupplier(
            @Valid @ModelAttribute("supplierDTO") SupplierDTO supplierDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Supplier existingSupplier = supplierService.getSupplier(supplierDTO.getSupplierID());
        if (existingSupplier == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Nhà cung cấp không tồn tại!");
            return "redirect:/dashboard/suppliers";
        }

        String currentEmail = existingSupplier.getEmail();
        if (!currentEmail.equals(supplierDTO.getEmail()) &&
                (supplierService.existsByEmail(supplierDTO.getEmail()) || iEmployeeService.existsByEmail(supplierDTO.getEmail()))) {
            bindingResult.rejectValue("email", "error.supplier", "Email đã tồn tại!");
        }

        supplierDTO.validate(supplierDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/update-supplier-form";
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.updateSupplier(supplier);

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Cập nhật nhà cung cấp thành công!");
        return "redirect:/dashboard/suppliers";
    }

    // Xóa nhà cung cấp - GET
    @GetMapping("/suppliers/delete/{id}")
    public String showDeleteSupplier(@PathVariable("id") Integer id, Model model) {
        Supplier supplier = supplierService.getSupplier(id);
        if (supplier == null) {
            return "redirect:/dashboard/suppliers";
        }
        model.addAttribute("supplier", supplier);
        return "dashboard/supplier/delete-supplier";
    }

    // Xóa nhà cung cấp - POST
    @PostMapping("/suppliers/delete/{id}")
    public String deleteSupplier(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Supplier supplier = supplierService.getSupplier(id);
        if (supplier == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Nhà cung cấp không tồn tại!");
        } else {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Xóa nhà cung cấp thành công!");
        }
        return "redirect:/dashboard/suppliers";
    }
}