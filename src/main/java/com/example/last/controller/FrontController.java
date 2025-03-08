package com.example.last.controller;

import com.example.last.model.Customer;
import com.example.last.model.Employee;
import com.example.last.service.CustomerService;
import com.example.last.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class FrontController {
    @Autowired
    CustomerService customerService;

    @RequestMapping({"", "/home", "/index"})
    public String home(Model model) {
        List<Customer> customers = customerService.findAllCustomers();
        model.addAttribute("customers", customers);
        return "testIndex";
    }


}
