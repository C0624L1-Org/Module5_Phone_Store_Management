package com.example.md5_phone_store_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/admin")
    public String admin(Model model) {
        return "dashboard/admin/admin-home";
    }

    @GetMapping("/warehouse-staff")
    public String warehouseStaff(Model model) {
        return "dashboard/warehouse-staff/warehouse-staff-home";
    }

    @GetMapping("/sales-staff")
    public String salesStaff(Model model) {
        return "dashboard/sales-staff/sales-staff-home";
    }

    @GetMapping("/sales-person")
    public String salesPerson(Model model) {
        return "dashboard/sales-person/sales-person-home";
    }

}