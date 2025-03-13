//package com.example.md5_phone_store_management.controller;
//
//import com.example.md5_phone_store_management.model.Employee;
//import com.example.md5_phone_store_management.model.Role;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//
//@Controller
//public class DashboardController {
//
//    // DashboardController.java
//    @GetMapping("/dashboard/admin/admin-home")
//    public String adminDashboard(HttpSession session, Model model) {
//        Employee employee = checkAuth(session, Role.Admin);
//        if (employee == null) return "redirect:/auth/login";
//
//        model.addAttribute("employee", employee);
//        return "dashboard/admin/admin-home";
//    }
//
//    @GetMapping("/dashboard/sales/sales-staff-home")
//    public String salesDashboard(HttpSession session, Model model) {
//        Employee employee = checkAuth(session, Role.SalesStaff);
//        if (employee == null) return "redirect:/auth/login";
//
//        model.addAttribute("employee", employee);
//        return "dashboard/sales-staff/sales-staff-home";
//    }
//
//    @GetMapping("/dashboard/business-staff/business-staff-home")
//    public String marketingDashboard(HttpSession session, Model model) {
//        Employee employee = checkAuth(session, Role.SalesPerson);
//        if (employee == null) return "redirect:/auth/login";
//
//        model.addAttribute("employee", employee);
//        return "dashboard/business-staff/business-staff-home";
//    }
//
//    @GetMapping("/dashboard/warehouse-staff/warehouse-staff-home")
//    public String warehouseDashboard(HttpSession session, Model model) {
//        Employee employee = checkAuth(session, Role.WarehouseStaff);
//        if (employee == null) return "redirect:/auth/login";
//
//        model.addAttribute("employee", employee);
//        return "dashboard/warehouse-staff/warehouse-staff-home";
//    }
//
//    private Employee checkAuth(HttpSession session, Role requiredRole) {
//        Employee employee = (Employee) session.getAttribute("employee");
//        if (employee == null || employee.getRole() != requiredRole) {
//            return null;
//        }
//        return employee;
//    }
//
//}