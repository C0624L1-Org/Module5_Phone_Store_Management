package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.implement.InvoiceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;

@Controller
@RequestMapping("/dashboard")
public class NewReportController {
    @Autowired
    InvoiceServiceImpl invoiceService;
    @GetMapping("/chart-sale")
    public ModelAndView chartSale(@RequestParam(name = "page", defaultValue = "0") int page
            ,@RequestParam(value = "year", required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        ModelAndView mv = new ModelAndView("dashboard/report-management/chart-sale");
        mv.addObject("receipt",invoiceService.findAllSuccessInvoices());
        mv.addObject("receiptForYear",invoiceService.getMonthlyRevenueByYear(year));
        mv.addObject("year", year);
        mv.addObject("page",page);
        return mv;
    }
}
