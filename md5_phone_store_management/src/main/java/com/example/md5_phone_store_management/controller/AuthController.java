package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        } else {
            Employee employee = new Employee();
            BeanUtils.copyProperties(employeeDTO, employee);

            if (!authService.register(employee)) {
                bindingResult.rejectValue("username", "", "Tài khoản hoặc email đã tồn tại!");
                return "auth/register";
            }

            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công!");
            return "redirect:/auth/login";
        }
    }


    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute("employee") != null) {
            Employee employee = (Employee) session.getAttribute("employee");
            return switch (employee.getRole()) {
                case Admin -> "redirect:/dashboard/admin/admin-home";
                case SalesStaff -> "redirect:/dashboard/sales/sales-staff-home";
                case SalesPerson -> "redirect:/dashboard/business-staff/business-staff-home";
                case WarehouseStaff -> "redirect:/dashboard/warehouse-staff/warehouse-staff-home";
            };
        }
        return "auth/login";
    }


    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        Optional<Employee> employeeOpt = authService.login(username, password);

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            session.setAttribute("employee", employee);

            return switch (employee.getRole()) {
                case Admin -> "redirect:/dashboard/admin/admin-home";
                case SalesStaff -> "redirect:/dashboard/sales/sales-staff-home";
                case SalesPerson -> "redirect:/dashboard/business-staff/business-staff-home";
                case WarehouseStaff -> "redirect:/dashboard/warehouse-staff/warehouse-staff-home";
            };
        } else {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Sai tài khoản hoặc mật khẩu!");
            return "redirect:/auth/login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
