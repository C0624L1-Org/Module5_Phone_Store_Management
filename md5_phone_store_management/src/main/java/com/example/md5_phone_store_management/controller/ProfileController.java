package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private IEmployeeRepository employeeRepository;

    @GetMapping("/common/profile")
    public String profile(Model model) {
        // Lấy thông tin người dùng hiện tại từ Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        // Lấy username của người dùng đã đăng nhập
        String username = authentication.getName();

        // Truy vấn thông tin employee từ database dựa trên username
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng với username: " + username));

        // Thêm thông tin employee vào model
        model.addAttribute("employee", employee);
        model.addAttribute("role", employee.getRole() != null ? employee.getRole().name() : "Không xác định");

        return "common/profile";
    }
}