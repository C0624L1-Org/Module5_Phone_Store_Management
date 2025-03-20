package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalesController {

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private IProductService iProductService;

//    @GetMapping("/dashboard/admin/sales")
//    public String sales(Model model) {
//
//    }
}
