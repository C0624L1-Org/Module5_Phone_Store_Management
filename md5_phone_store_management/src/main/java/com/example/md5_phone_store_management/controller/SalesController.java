package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.*;
import com.example.md5_phone_store_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.ArrayList;
import java.util.List;

@RequestMapping("/dashboard/sales")
@Controller
public class SalesController {

    @Autowired
    private ICustomerService iCustomerService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IInvoiceService iInvoiceService;

    @GetMapping("/form")
    public String openSalesForm(@RequestParam(name = "page", defaultValue = "0", required = false) int page, Model model) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Customer> customerList = iCustomerService.findAllCustomers(pageable);
        Page<Product> productList = iProductService.findAll(pageable);
        model.addAttribute("customerList", customerList);
        model.addAttribute("productList", productList);
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("invoiceDetail", new InvoiceDetail());
        return "dashboard/sales/form";
    }

    @PostMapping("/add")
    public String testSubmitForm(@ModelAttribute("invoice") Invoice invoice,
                               @RequestParam("productID") List<Integer> productID,
                               @RequestParam("quantity") List<Integer> quantity,
                               Model model) {

        // kiem tra khach hang moi hay cu
        if (invoice.getCustomer().getCustomerID() != null) {
            invoice.setCustomer(iCustomerService.findCustomerById(invoice.getCustomer().getCustomerID()));
        } else {
            Customer customer = new Customer();
            customer.setFullName(invoice.getCustomer().getFullName());
            customer.setEmail(invoice.getCustomer().getEmail());
            customer.setPhone(invoice.getCustomer().getPhone());
            customer.setAddress(invoice.getCustomer().getAddress());
            customer.setGender(Gender.Other);
            customer.setPurchaseCount(0);
            try {
                Customer newCustomer = iCustomerService.saveCustomer(customer); //dang loi cho nay
                System.out.println(newCustomer);
                invoice.setCustomer(newCustomer);
            } catch (Exception e) {
                model.addAttribute("messageType","error");
                model.addAttribute("message","Email hoặc SĐT đã tồn tại");
                System.out.println(e.getMessage());
                return "redirect:/dashboard/sales/form";
            }
        }
        /*
        * Duy Tan viet code luu thong tin invoice vao DB
        * */
        invoice = iInvoiceService.saveInvoice(invoice);
        System.out.println(invoice.toString());
        return null;
    }
}
