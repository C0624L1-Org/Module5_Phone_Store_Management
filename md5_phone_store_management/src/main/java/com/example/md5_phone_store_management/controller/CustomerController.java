package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.implement.CustomerServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private CustomerServiceImpl customerServiceWithJpa;


    @GetMapping("/admin/customers/list")
    public String listCustomers(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String phone) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerServiceWithJpa.findAllCustomers(pageable);

        model.addAttribute("customerPage", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", customerPage.getTotalPages());

        return "dashboard/admin/customers/list-customer";
    }



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






    @GetMapping("/admin/customers/search")
    public String searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            Model model, HttpSession session) {

        List<Customer> customers;

        if ((name == null || name.trim().isEmpty()) &&
                (phone == null || phone.trim().isEmpty()) &&
                (gender == null || gender.trim().isEmpty())) {

            customers = customerService.findAllCustomers();
            session.setAttribute("ERROR_MESSAGE", "Vui lòng nhập ít nhất một tiêu chí tìm kiếm!");
        } else {
            System.out.println("Tên : " + name);
            System.out.println("SĐT : " + phone);
            System.out.println("Giới tính : " + gender);

            customers = customerService.searchCustomers(name, phone, gender);

            if (customers.isEmpty()) {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy khách hàng nào!");
            } else {
                session.setAttribute("SUCCESS_MESSAGE", "Tìm thấy " + customers.size() + " khách hàng!");
            }
        }

        model.addAttribute("customers", customers);
        model.addAttribute("gender", gender);
        return "dashboard/admin/customers/list-customer";
    }





}
