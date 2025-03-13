package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.service.CustomerService;
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

    @GetMapping("/admin/customers/list")
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.findAllCustomers();
        model.addAttribute("customers", customers);
        return "dashboard/admin/customers/list-customer";
    }

    @GetMapping("/admin/customers/create")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "dashboard/admin/customers/create-customer";
    }

    @PostMapping("/admin/customers/create")
    public String createCustomer(@ModelAttribute Customer customer,
                                 RedirectAttributes redirectAttributes) {
        customerService.addNewCustomer(customer);
        redirectAttributes.addFlashAttribute("message", "Thêm khách hàng thành công.");
        return "redirect:/dashboard/admin/customers/list";
    }


    @GetMapping("/admin/customers/search")
    public String searchCustomers(@RequestParam("search-type") String searchType,
                                  @RequestParam("keyWord") String keyWord,
                                  Model model) {
        List<Customer> customers = customerService.searchCustomers(searchType, keyWord);
        model.addAttribute("customers", customers);
        return "dashboard/admin/customers/list-customer";
    }


    @GetMapping("/admin/customers/edit/{id}")
    public String editCustomer(@PathVariable Integer id, Model model) {
        Customer customer = customerService.getCustomerByID(id);
        if (customer == null) {
            model.addAttribute("error", "Khách hàng không tồn tại.");
            return "redirect:dashboard/admin/customers/list";
        }
        model.addAttribute("customer", customer);
        return "dashboard/admin/customers/update-customer";
    }

    @PostMapping("/admin/customers/update")
    public String updateCustomer(@RequestParam Integer customerID, @ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        System.out.println("Updating customer with ID: " + customerID);
        Customer customerNeedUpdate = customerService.getCustomerByID(customerID);
        if (customerNeedUpdate == null) {
            redirectAttributes.addFlashAttribute("error", "Khách hàng không tồn tại.");
            return "redirect:/dashboard/admin/customers/list";
        }
        BeanUtils.copyProperties(customer, customerNeedUpdate, "customerID");
        boolean isUpdated = customerService.updateCustomer(customerNeedUpdate);
        if (isUpdated) {
            redirectAttributes.addFlashAttribute("message", "Cập nhật khách hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật khách hàng.");
        }
        return "redirect:/dashboard/admin/customers/list";
    }


    @PostMapping("/admin/customers/delete")
    public String deleteCustomers(@RequestParam List<Integer> ids) {
        for (Integer customerID : ids) {
            customerService.deleteCustomer(Collections.singletonList(customerID));
        }
        System.out.println("đã chạy qua pt xóa ");
        return "redirect:/dashboard/admin/customers/list";
    }


}
