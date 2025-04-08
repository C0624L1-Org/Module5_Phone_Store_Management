package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import com.example.md5_phone_store_management.service.implement.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private ISupplierService iSupplierService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private ICustomerService iCustomerService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/admin")
    public String admin(Model model) {
        // Thống kê nhân viên
        model.addAttribute("countEmployee", iEmployeeService.countEmployee());
        model.addAttribute("countSalesStaff", iEmployeeService.countSalesStaff());
        model.addAttribute("countBusinessStaff", iEmployeeService.countBusinessStaff());
        model.addAttribute("countWarehouseStaff", iEmployeeService.countWarehouseStaff());
        model.addAttribute("countCustomer", iCustomerService.countTotalCustomers());

        // Thống kê nhà cung cấp
        model.addAttribute("countSuppliers", iSupplierService.countSuppliers());

        // Thống kê sản phẩm và doanh thu
        model.addAttribute("countProducts", iProductService.countProducts());
        model.addAttribute("totalProductsSold", iProductService.countSoldProducts());
//        model.addAttribute("totalRevenue", iProductService.calculateTotalRevenue());


        // Lịch sử thay đổi
        model.addAttribute("recentEmployeeChanges", auditLogService.getRecentChanges("Employee", 5));
        model.addAttribute("recentSupplierChanges", auditLogService.getRecentChanges("Supplier", 5));
        model.addAttribute("recentProductChanges", auditLogService.getRecentChanges("Product", 5));


        return "dashboard/admin/ttlog";
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
