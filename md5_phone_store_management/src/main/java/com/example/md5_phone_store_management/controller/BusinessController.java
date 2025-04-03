package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.service.IInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                                  @RequestParam(name = "sortValue", required = false) String sortValue,
                                  @RequestParam(name = "sortType", defaultValue = "true", required = false) boolean sortType,
                                  @RequestParam(name = "remain", defaultValue = "false", required = false) boolean remain,
                                  Model model) {

        Pageable pageable = PageRequest.of(page, 2);
        Page<Invoice> invoicesPage;
        if (sortValue != null) {
            invoicesPage = returnSortedInvoicePage(pageable, sortType, sortValue);
        } else {
            invoicesPage = invoiceService.findAllSuccessInvoices(pageable);
        }
        model.addAttribute("invoices", invoicesPage);
        model.addAttribute("sortType", sortType);
        model.addAttribute("sortValue", sortValue);
        model.addAttribute("currentPage", page);

        return "dashboard/business-management/transaction";
    }

    public Page<Invoice> returnSortedInvoicePage(Pageable pageable, boolean sortType, String sortValue) {
        System.out.println("--- Running returnSortedInvoicePage ---");
        System.out.println(sortType);
        switch (sortValue) {
            case "time":
                System.out.println("time");
                return sortType ? invoiceService.findAllSuccessInvoicesWithTimeAsc(pageable) : invoiceService.findAllSuccessInvoicesWithTimeDesc(pageable);
            case "customer":
                System.out.println("customer");
                return sortType ? invoiceService.findAllSuccessInvoicesWithCustomerNameAsc(pageable) : invoiceService.findAllSuccessInvoicesWithCustomerNameDesc(pageable);
            case "product":
                System.out.println("product");
                return sortType ? invoiceService.findAllSuccessInvoicesWithProductNameAsc(pageable) : invoiceService.findAllSuccessInvoicesWithProductNameDesc(pageable);
            case "quantity":
                System.out.println("quantity");
                return sortType ? invoiceService.findAllSuccessInvoicesWithQuantityAsc(pageable) : invoiceService.findAllSuccessInvoicesWithQuantityDesc(pageable);
            case "amount":
                System.out.println("amount");
                return sortType ? invoiceService.findAllSuccessInvoicesWithAmountAsc(pageable) : invoiceService.findAllSuccessInvoicesWithAmountDesc(pageable);
            default:
                System.out.println("default");
                return invoiceService.findAllSuccessInvoices(pageable);
        }

    }
}
