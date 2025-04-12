package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.ChangeLog;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.ISupplierService;
import com.example.md5_phone_store_management.service.implement.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/changelogs")
public class ChangeLogController {
    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private ISupplierService iSupplierService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private ICustomerService iCustomerService;

    @Autowired
    private ChangeLogService changeLogService;

    @GetMapping("/notification")
    public List<ChangeLog> getAllChangeLogs() {
        return changeLogService.getAllChangeLogs();
    }

    //    tất cả
    @GetMapping("/count/all/employees")
    public Long getEmployeeLog() {
        return iEmployeeService.countEmployee();
    }

    @GetMapping("/count/all/customers")
    public Integer countTotalCustomers() {
        return iCustomerService.countTotalCustomers();
    }

    @GetMapping("/count/all/suppliers")
    public Long countSuppliers() {
        return iSupplierService.countSuppliers();
    }

    @GetMapping("/count/all/products")
    public Long countProducts() {
        return iProductService.countProducts();
    }

    @GetMapping("/count/all/total-revenue")
    public ResponseEntity<Double> calculateTotalRevenue() {
        try {
            Long soldProducts = iProductService.countSoldProducts();
            return ResponseEntity.ok(soldProducts != null ? soldProducts.doubleValue() : 0.0);
        } catch (Exception e) {
            System.err.println("Lỗi tính doanh thu: " + e.getMessage());
            return ResponseEntity.ok(0.0);
        }
    }

    //    cập nhật lần cuối
    @GetMapping("/lastUpdate/time/employees")
    public LocalDateTime getLastEmployeeUpdateTime() {
        return changeLogService.getLastUpdateTime("employee");
    }

    @GetMapping("/lastUpdate/time/customers")
    public LocalDateTime getLastCustomerUpdateTime() {
        return changeLogService.getLastUpdateTime("customer");
    }

    @GetMapping("/lastUpdate/time/suppliers")
    public LocalDateTime getLastSupplierUpdateTime() {
        return changeLogService.getLastUpdateTime("supplier");
    }

    @GetMapping("/lastUpdate/time/products")
    public LocalDateTime getLastProductUpdateTime() {
        return changeLogService.getLastUpdateTime("product");
    }

    @GetMapping("/lastUpdate/time/total-revenue")
    public LocalDateTime getLastRevenueUpdateTime() {
        return changeLogService.getLastUpdateTime("invoices");
    }


    //    đã bán
    @GetMapping("/count/sold/products")
    public Long countSoldProducts() {
        Long soldProducts = iProductService.countSoldProducts();
        return soldProducts != null ? soldProducts : 0L;
    }


//@GetMapping("/count/all/sales-staff")
//    public Long countSalesStaff() {
//        return iEmployeeService.countSalesStaff();
//    }
//
//    @GetMapping("/count/all/business-staff")
//    public Long countBusinessStaff() {
//        return iEmployeeService.countBusinessStaff();
//    }
//
//    @GetMapping("/count/all/warehouse-staff")
//    public Long countWarehouseStaff() {
//        return iEmployeeService.countWarehouseStaff();
//    }

}

