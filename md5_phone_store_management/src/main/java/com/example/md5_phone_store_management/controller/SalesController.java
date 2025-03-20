package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/dashboard/sales")
@Controller
public class SalesController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private IProductService iProductService;

    @GetMapping("/management")
    public String openSalesForm(Model model) {
        List<Customer> customerList = customerService.findAllCustomers();
        model.addAttribute("customerList", customerList);
        model.addAttribute("productList", iProductService.findAllProducts());
        model.addAttribute("invoice", new Invoice());
        for (Customer customer : customerList) {
            System.out.println(customer.toString());
        }
        return "dashboard/sales/form";
    }

    @PostMapping("/add")
    public void testSubmitForm(@ModelAttribute("invoice") Invoice invoice, Model model) {
        System.out.println(invoice.toString());
    }
}
