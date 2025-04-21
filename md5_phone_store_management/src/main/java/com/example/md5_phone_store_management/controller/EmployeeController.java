package com.example.md5_phone_store_management.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.implement.SupplierService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/dashboard")
public class EmployeeController {

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private GlobalControllerAdvice globalControllerAdvice;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ICustomerService customerService;

    //Read(a Đình Anh)
    @GetMapping("/admin/employees/list")
    public ModelAndView getListEmployees(@RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        ModelAndView mv = new ModelAndView("dashboard/admin/employees/list-employee");
        Pageable pageable = PageRequest.of(page, 5);
        mv.addObject("currentPage", page);
        mv.addObject("employeePage", iEmployeeService.getAllEmployeesExceptAdmin(pageable));
        mv.addObject("totalPage", iEmployeeService.getAllEmployees(pageable).getTotalPages());
        return mv;
    }

    @GetMapping("/admin/employees/search")
    public ModelAndView searchEmployees(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String role,
                                        @RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        ModelAndView mv = new ModelAndView("dashboard/admin/employees/list-employee");
        Pageable pageable = PageRequest.of(page, 5);
        if ((name == null || name.isBlank()) &&
                (phone == null || phone.isBlank()) &&
                (role == null || role.isBlank())) {
            return new ModelAndView("redirect:/dashboard/admin/employees/list?page=" + page);
        }
        name = (name != null) ? name.trim() : null;
        phone = (phone != null) ? phone.trim() : null;
        role = (role != null) ? role.trim() : null;

        Page<Employee> employeePage = iEmployeeService.searchEmployees(name, phone, role, pageable);

        mv.addObject("currentPage", page);
        mv.addObject("employeePage", employeePage);
        mv.addObject("totalPage", employeePage.getTotalPages());
        mv.addObject("name", name);
        mv.addObject("phone", phone);
        mv.addObject("role", role);

        if (employeePage.getTotalElements() == 0) {
            mv.addObject("messageType", "error");
            mv.addObject("role", role);
            mv.addObject("message", "Không tìm thấy kết quả phù hợp với tìm kiếm");
        }

        return mv;
    }


    //Create(Tuấn Anh)
    @GetMapping("/admin/employees/create")
    public String employees(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        model.addAttribute("roles", Role.values());
        return "dashboard/admin/employees/create-employee";
    }

    @PostMapping("/admin/employees/create")
    public String createEmployee(@Valid EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        System.out.println(employeeDTO.getFullName());
        if (iEmployeeService.existsByUsername(employeeDTO.getUsername())) {
            bindingResult.rejectValue("username", "", "Tài khoản này đã tồn tại!");
        }

        if (iEmployeeService.existsByEmail(employeeDTO.getEmail()) ||
                customerService.isEmailExists(employeeDTO.getEmail()) ||
                supplierService.existsByEmail(employeeDTO.getEmail())) {
            bindingResult.rejectValue("email", "", "Email này đã tồn tại!");
        }

        if(supplierService.existsByPhone(employeeDTO.getPhone()) ||
                customerService.isPhoneExists(employeeDTO.getPhone()) ||
                iEmployeeService.existsByPhone(employeeDTO.getPhone())) {
            bindingResult.rejectValue("phone", "", "Số điện thoại này đã tồn tại!");
        }

        employeeDTO.validate(employeeDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeDTO", employeeDTO);
            model.addAttribute("roles", Role.values());
            return "dashboard/admin/employees/create-employee";
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        iEmployeeService.addEmployee(employee);

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Thêm mới nhân viên thành công!");
        return "redirect:/dashboard/admin/employees/list";

    }


    // UPDATE(Tân)
    @GetMapping("/admin/employees/edit/{employeeID}")
    public String showUpdateForm(@PathVariable("employeeID") Integer employeeID,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Employee employee = iEmployeeService.getEmployeeById(employeeID);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Người dùng không tồn tại");
            return "redirect:/dashboard/admin/employees/list";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("roles", Role.values());

        return "dashboard/admin/employees/update-employee";
    }

    @PostMapping("/admin/employees/edit/{employeeID}")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 @PathVariable("employeeID") Integer employeeID,
                                 @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Employee employeeToUpdate = iEmployeeService.getEmployeeById(employeeDTO.getEmployeeID());

        if (!employeeToUpdate.getUsername().equals(employeeDTO.getUsername())
                && iEmployeeService.existsByUsername(employeeDTO.getUsername())) {
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", Role.values());
            bindingResult.rejectValue("username", "", "Tài khoản này đã tồn tại!");
        }

        // Kiểm tra email
        if (!employeeToUpdate.getEmail().equals(employeeDTO.getEmail()) &&
                (iEmployeeService.existsByEmail(employeeDTO.getEmail()) ||
                        customerService.isEmailExists(employeeDTO.getEmail()) ||
                        supplierService.existsByEmail(employeeDTO.getEmail()))) {
            bindingResult.rejectValue("email", "", "Email này đã tồn tại!");
        }

        // Kiểm tra phone
        if (!employeeToUpdate.getPhone().equals(employeeDTO.getPhone()) &&
                (iEmployeeService.existsByPhone(employeeDTO.getPhone()) ||
                        customerService.isPhoneExists(employeeDTO.getPhone()) ||
                        supplierService.existsByPhone(employeeDTO.getPhone()))) {
            bindingResult.rejectValue("phone", "", "Số điện thoại này đã tồn tại!");
        }

        employeeDTO.validate(employeeDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", Role.values());
            return "dashboard/admin/employees/update-employee";
        }

        int updatedRows = iEmployeeService.updateEmployee(
                employeeDTO.getEmployeeID(),
                employeeDTO.getFullName(),
                employeeDTO.getDob(),
                employeeDTO.getAddress(),
                employeeDTO.getPhone(),
                employeeDTO.getRole(),
                employeeDTO.getEmail()
        );

        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (avatarFile.getSize() > 10 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Kích thước ảnh quá lớn!");
                model.addAttribute("employee", employeeDTO);
                model.addAttribute("roles", Role.values());
                return "redirect:/dashboard/admin/employees/edit/" + employeeID;
            }
            if (!avatarFile.getContentType().startsWith("image/")) {
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Định dạng ảnh không hợp lệ!");
                model.addAttribute("employee", employeeDTO);
                model.addAttribute("roles", Role.values());
                return "redirect:/dashboard/admin/employees/edit/" + employeeID;
            }
            iEmployeeService.updateAvatar(employeeID, avatarFile);
        }

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Cập nhật người dùng thành công");

        return "redirect:/dashboard/admin/employees/list";
    }

    //delete
    @GetMapping("/admin/employees/delete/{ids}")
    public String deleteEmployee(@PathVariable List<Integer> ids, Model model, RedirectAttributes redirectAttributes) {
        List<Integer> defaultAccounts = iEmployeeService.findEmployeeIDOfDefaultAccount();
        for (Integer id : ids) {
            System.out.println("ID của employee: " + id);
            if (defaultAccounts.contains(id)) {
                System.out.println("bị trùng");
                redirectAttributes.addFlashAttribute("messageType", "error");
                redirectAttributes.addFlashAttribute("message", "Bạn không thể xóa các tài khoản mặc định!");
                return "redirect:/dashboard/admin/employees/list";
            }
        }
        try {
            iEmployeeService.deleteEmployeesById(ids);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Xóa nhân viên thành công");
        } catch (Exception e) {
            System.out.println("Không tể xóa nhân vvien6n: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Xóa nhân viên không thành công! Vui lòng thử lại!");
        }

        return "redirect:/dashboard/admin/employees/list";
    }
}
