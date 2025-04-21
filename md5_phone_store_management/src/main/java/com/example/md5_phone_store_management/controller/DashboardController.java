package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import com.example.md5_phone_store_management.service.implement.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/admin")
    public String admin() {
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
