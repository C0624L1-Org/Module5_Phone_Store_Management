package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.IInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard/business")
public class BusinessController {
    @Autowired
    private IInvoiceService invoiceService;

    @GetMapping("/management")
    public String index() {
        return "dashboard/business-management/management";
    }

    @GetMapping("/transaction")
    public String transactionPage(Model model) {
        return "dashboard/business-management/transaction";
    }
}
