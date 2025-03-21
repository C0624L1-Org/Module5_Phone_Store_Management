package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
@Controller
@RequestMapping("/dashboard")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/admin/customers/delete")
    public String deleteCustomers(@RequestParam List<Integer> ids, HttpSession session) {
        for (Integer customerID : ids) {
            customerService.deleteCustomer(Collections.singletonList(customerID));
        }
        session.setAttribute("SUCCESS_MESSAGE", "Đã Xóa " +  ids.size() + " khách hàng!");
        return "redirect:/dashboard/admin/customers/list";
    }

    @GetMapping("/admin/customers/add")
    public String employees(Model model) {
        model.addAttribute("customer", new Customer());
        return "dashboard/admin/customers/create-customer";
    }


    @GetMapping("/admin/customers/showEdit/{customerID}")
    public String showUpdateForm(@PathVariable("customerID") Integer customerID,
                                 Model model, HttpSession session) {
        Customer customer = customerService.getCustomerByID(customerID);
        if (customer == null) {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy khách hàng này!");
            return "redirect:/dashboard/admin/customers/list";
        }
        model.addAttribute("customer", customer);
        return "dashboard/admin/customers/update-customer";
    }



    @GetMapping("/admin/customers/list")
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.findAllCustomers();
        model.addAttribute("customers", customers);
        return "dashboard/admin/customers/list-customer";
    }


    @GetMapping("/admin/customers/search")
    public String searchCustomers(@RequestParam("search-type") String searchType,
                                  @RequestParam("keyWord") String keyWord,
                                  Model model, HttpSession session ){
        List<Customer> customers;
        if (searchType.equals("gender") && keyWord == null) {
            customers = customerService.findAllCustomers();
        } else {
            customers = customerService.searchCustomers(searchType, keyWord);
        }
        int totalSearchCustomers = customers.size();
        model.addAttribute("customers", customers);
        if (keyWord == null || keyWord.trim().isEmpty()) {
            session.setAttribute("ERROR_MESSAGE", "Vui lòng nhập từ khóa để tìm kiếm!");
        } else if (totalSearchCustomers == 0) {
            session.setAttribute("ERROR_MESSAGE", "Không tìm thấy khách hàng nào!");
        } else {
            session.setAttribute("SUCCESS_MESSAGE", "Tìm thấy " + totalSearchCustomers + " khách hàng!");
        }
        return "dashboard/admin/customers/list-customer";
    }




}
