package com.example.last.controller;

import com.example.last.model.Customer;
import com.example.last.model.Gender;
import com.example.last.service.CustomerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;


    @GetMapping
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.findAllCustomers();
        model.addAttribute("customers", customers);
        return "customer/customerList";
    }
    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/customerAdd";
    }



    @PostMapping("/create")
    public String createCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        customerService.addNewCustomer(customer);
        redirectAttributes.addFlashAttribute("message", "Thêm khách hàng thành công.");
        return "redirect:/customers";
    }



    @GetMapping("/search")
    public String searchCustomers(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) String email,
                                  Model model) {
        List<Customer> customers = customerService.searchCustomers(name, phone, email);
        model.addAttribute("customers", customers);
        return "customer/customerList";
    }



    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Integer id, Model model) {
        Customer customer = customerService.getCustomerByID(id);
        if (customer == null) {
            model.addAttribute("error", "Khách hàng không tồn tại.");
            return "redirect:/customers";
        }
        model.addAttribute("customer", customer);
        return "customer/customerEdit";
    }

    @PostMapping("/update")
    public String updateCustomer(@RequestParam Integer customerID, @ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        System.out.println("Updating customer with ID: " + customerID);
        Customer customerNeedUpdate = customerService.getCustomerByID(customerID);
        if (customerNeedUpdate == null) {
            redirectAttributes.addFlashAttribute("error", "Khách hàng không tồn tại.");
            return "redirect:/customers";
        }
        BeanUtils.copyProperties(customer, customerNeedUpdate, "customerID");
        boolean isUpdated = customerService.updateCustomer(customerNeedUpdate);
        if (isUpdated) {
            redirectAttributes.addFlashAttribute("message", "Cập nhật khách hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật khách hàng.");
        }
        return "redirect:/customers";
    }




    @PostMapping("/delete")
    public String deleteCustomers(@RequestParam List<Integer> ids) {
        for (Integer customerID : ids) {
            customerService.deleteCustomer(Collections.singletonList(customerID));
        }
        return "redirect:/customers";
    }






}