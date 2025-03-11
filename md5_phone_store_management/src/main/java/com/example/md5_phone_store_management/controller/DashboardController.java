package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private IEmployeeRepository employeeRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
        return "dashboard/admin/admin-home";
    }

    @GetMapping("/sales-staff")
    public String salesStaffDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
        return "dashboard/sales-staff/sales-staff-home";
    }

    @GetMapping("/business-staff")
    public String salesPersonDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
        return "dashboard/business-staff/business-staff-home";
    }

    @GetMapping("/warehouse-staff")
    public String warehouseStaffDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
        return "dashboard/warehouse-staff/warehouse-staff-home";
    }

    @GetMapping
    public String defaultDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
        return "/home";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("employee", employee);
        model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
        return "dashboard/profile";
    }
}