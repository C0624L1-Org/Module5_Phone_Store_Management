package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.IEmployeeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LoginController {
    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Thêm passwordEncoder để kiểm tra mật khẩu

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("username") String username,
                        @ModelAttribute("password") String password,
                        HttpSession session,
                        Model model) {
        Optional<Employee> employeeOpt = iEmployeeService.findByUsername(username);

        if (employeeOpt.isEmpty() || !passwordEncoder.matches(password, employeeOpt.get().getPassword())) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
            return "auth/login";
        }

        Employee employee = employeeOpt.get();
        session.setAttribute("employee", employee);
        session.setAttribute("role", employee.getRole().name());

        // Điều hướng theo vai trò
        return switch (employee.getRole()) {
            case Admin -> "redirect:/dashboard/admin";
            case SalesStaff -> "redirect:/dashboard/sales-staff";
            case SalesPerson -> "redirect:/dashboard/business-staff";
            case WarehouseStaff -> "redirect:/dashboard/warehouse-staff";
            default -> "redirect:/dashboard";
        };
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (iEmployeeService.existsByUsername(employeeDTO.getUsername())) {
            bindingResult.rejectValue("username", "", "Tài khoản đã tồn tại!");
        }

        if (iEmployeeService.existsByEmail(employeeDTO.getEmail())) {
            bindingResult.rejectValue("email", "", "Email đã tồn tại!");
        }

        employeeDTO.validate(employeeDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeDTO", employeeDTO);
            return "auth/register";
        }

        Employee newEmployee = new Employee();
        BeanUtils.copyProperties(employeeDTO, newEmployee);

        // Mã hóa mật khẩu trước khi lưu vào database
        newEmployee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

        iEmployeeService.addEmployee(newEmployee);
        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Hãy đăng nhập.");

        return "redirect:/login";
    }
}
