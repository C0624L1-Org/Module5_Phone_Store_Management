package com.example.md5_phone_store_management.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.SupplierDTO;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.implement.SupplierService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/dashboard")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IEmployeeService employeeService;

    @GetMapping("/suppliers")
    public String getAllSuppliers(
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "address", required = false, defaultValue = "") String address,
            @RequestParam(value = "phone", required = false, defaultValue = "") String phone,
            @RequestParam(value = "email", required = false, defaultValue = "") String email,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        Page<Supplier> suppliers;

        if (name.isEmpty() && address.isEmpty() && phone.isEmpty() && email.isEmpty()) {
            suppliers = supplierService.getAllSuppliers(page, size);
        } else {
            suppliers = supplierService.searchSuppliers(name, address, phone, email, page, size);
        }

        model.addAttribute("suppliers", suppliers.getContent());
        model.addAttribute("totalPages", suppliers.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("name", name);
        model.addAttribute("address", address);
        model.addAttribute("phone", phone);
        model.addAttribute("email", email);

        return "dashboard/supplier/supplier-list";
    }

    @GetMapping("/suppliers/create")
    public String createSupplier(Model model) {
        model.addAttribute("supplierDTO", new SupplierDTO());
        return "dashboard/supplier/create-supplier";
    }

    @PostMapping("/suppliers/create")
    public String createSupplier(@Valid @ModelAttribute("supplierDTO") SupplierDTO supplierDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/create-supplier";
        }

        // Kiểm tra email đã tồn tại
        if(supplierService.existsByEmail(supplierDTO.getEmail()) || 
           customerService.isEmailExists(supplierDTO.getEmail()) || 
           employeeService.existsByEmail(supplierDTO.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Email này đã được sử dụng");
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/create-supplier";
        }
        
        // Kiểm tra số điện thoại đã tồn tại
        if(supplierService.existsByPhone(supplierDTO.getPhone()) || 
           customerService.isPhoneExists(supplierDTO.getPhone()) || 
           employeeService.existsByPhone(supplierDTO.getPhone())) {
            bindingResult.rejectValue("phone", "duplicate", "Số điện thoại này đã được sử dụng");
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/create-supplier";
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.saveSupplier(supplier);
        
        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Thêm mới Nhà cung cấp thành công");

        return "redirect:/dashboard/suppliers";
    }

    @GetMapping("/update-supplierForm/{id}")
    public String updateSupplier(@PathVariable("id") Integer id, Model model) {
        Supplier supplier = supplierService.getSupplier(id);
        SupplierDTO supplierDTO = new SupplierDTO();
        BeanUtils.copyProperties(supplier, supplierDTO);
        model.addAttribute("supplierDTO", supplierDTO);
        return "dashboard/supplier/update-supplier-form";
    }

    @PostMapping("/update-supplier")
    public String changeInformSupplier(@Valid @ModelAttribute("supplierDTO") SupplierDTO supplierDTO,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/update-supplier-form";
        }

        Supplier existingSupplier = supplierService.getSupplier(supplierDTO.getSupplierID());
        
        // Kiểm tra email trùng lặp nếu email thay đổi
        if (!existingSupplier.getEmail().equals(supplierDTO.getEmail())) {
            // Kiểm tra trùng email với nhà cung cấp khác, khách hàng hoặc nhân viên
            if(supplierService.existsByEmail(supplierDTO.getEmail()) || 
               customerService.isEmailExists(supplierDTO.getEmail()) || 
               employeeService.existsByEmail(supplierDTO.getEmail())) {
                bindingResult.rejectValue("email", "duplicate", "Email này đã được sử dụng");
                model.addAttribute("supplierDTO", supplierDTO);
                return "dashboard/supplier/update-supplier-form";
            }
        }
        
        // Kiểm tra số điện thoại trùng lặp nếu số điện thoại thay đổi
        if (!existingSupplier.getPhone().equals(supplierDTO.getPhone())) {
            // Kiểm tra trùng số điện thoại với nhà cung cấp khác, khách hàng hoặc nhân viên
            if(supplierService.existsByPhone(supplierDTO.getPhone()) || 
               customerService.isPhoneExists(supplierDTO.getPhone()) || 
               employeeService.existsByPhone(supplierDTO.getPhone())) {
                bindingResult.rejectValue("phone", "duplicate", "Số điện thoại này đã được sử dụng");
                model.addAttribute("supplierDTO", supplierDTO);
                return "dashboard/supplier/update-supplier-form";
            }
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.updateSupplier(supplier);
        
        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Cập nhật Nhà cung cấp thành công");

        return "redirect:/dashboard/suppliers";
    }
}