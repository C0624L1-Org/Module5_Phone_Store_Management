package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.service.IInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String transactionPage(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                  Model model) {
        List<Invoice> invoices = invoiceService.findAll();
        for(Invoice i : invoices) {
            System.out.println(i.toString());
        }
        return "dashboard/business-management/transaction";
    }
}
