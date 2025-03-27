package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.SupplierDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // Phương thức hiển thị danh sách nhà cung cấp có phân trang và tìm kiếm
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

    // Phương thức tạo nhà cung cấp mới
    @GetMapping("/suppliers/create")
    public String createSupplier(Model model) {
        model.addAttribute("supplierDTO", new SupplierDTO());
        return "dashboard/supplier/create-supplier";
    }

    @PostMapping("/suppliers/create")
    public String createSupplier(@Valid @ModelAttribute("supplierDTO") SupplierDTO supplierDTO,
                                 BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/create-supplier";
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.saveSupplier(supplier);

        return "redirect:/dashboard/suppliers";
    }

    // Phương thức cập nhật nhà cung cấp
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
                                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierDTO", supplierDTO);
            return "dashboard/supplier/update-supplier-form";
        }

        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierDTO, supplier);
        supplierService.updateSupplier(supplier);

        return "redirect:/dashboard/suppliers";
    }

    // Phương thức xóa nhà cung cấp
    @GetMapping("/suppliers/delete/{ids}")
    public String deleteSuppliers(@PathVariable("ids") String ids, RedirectAttributes redirectAttributes) {
        try {
            // Chuyển chuỗi ids thành List<Integer>
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            // Gọi service để xóa
            supplierService.deleteSupplier(idList);

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("message", "Xóa nhà cung cấp thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            // Thêm thông báo lỗi nếu xóa thất bại
            redirectAttributes.addFlashAttribute("message", "Lỗi khi xóa nhà cung cấp: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/dashboard/suppliers";
    }
}