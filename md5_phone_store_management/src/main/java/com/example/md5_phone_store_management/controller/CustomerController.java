package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.CustomerService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.implement.CustomerServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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

    @Autowired
    private IInvoiceService invoiceService;


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
//        xử lý sai logic tìm kiếm bị trùng toast
        model.addAttribute("cP", "list");

        return "dashboard/admin/customers/list-customer";
    }


    @GetMapping("/admin/customers/delete")
    public String deleteCustomers(@RequestParam List<Integer> ids, HttpSession session) {
        for (Integer customerID : ids) {
            customerService.deleteCustomer(Collections.singletonList(customerID));
        }
        session.setAttribute("SUCCESS_MESSAGE", "Đã Xóa " + ids.size() + " khách hàng!");
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

    /**
     * Xem danh sách hóa đơn của khách hàng
     */
    @GetMapping("/admin/customers/{customerID}/invoices")
    public String viewCustomerInvoices(@PathVariable("customerID") Integer customerID,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       RedirectAttributes redirectAttributes,
                                       Model model,
                                       HttpSession session) {
        // Tìm khách hàng
        Customer customer = customerService.getCustomerByID(customerID);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy khách hàng này!");
            return "redirect:/dashboard/admin/customers/list";
        }

        // Lấy danh sách hóa đơn của khách hàng
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoicePage = invoiceService.findByCustomerId(customerID, pageable);

        model.addAttribute("customer", customer);
        model.addAttribute("invoicePage", invoicePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", invoicePage.getTotalPages());

        return "dashboard/admin/customers/customer-invoices";
    }


    @GetMapping("/admin/customers/search")
    public ModelAndView searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            HttpSession session) {

        ModelAndView mv = new ModelAndView("dashboard/admin/customers/list-customer");
        Pageable pageable = PageRequest.of(page, 5);

        // Trim các tham số tìm kiếm
        name = (name != null) ? name.trim() : null;
        phone = (phone != null) ? phone.trim() : null;
        gender = (gender != null) ? gender.trim() : null;

        if ((name == null || name.trim().isEmpty()) &&
                (phone == null || phone.trim().isEmpty()) &&
                (gender == null || gender.trim().isEmpty())) {
            mv.addObject("cP", "list");

            Page<Customer> customerPage = customerServiceWithJpa.findAllCustomers(pageable);

            mv.addObject("customerPage", customerPage);
            mv.addObject("currentPage", page);
            mv.addObject("totalPage", customerPage.getTotalPages());



            session.setAttribute("ERROR_MESSAGE", "Vui lòng nhập ít nhất một tiêu chí tìm kiếm!");
        } else {

            Page<Customer> customerPage = customerServiceWithJpa.searchCustomers(name, phone, gender, pageable);

            System.out.println("Số lượng khách hàng tìm thấy: " + customerPage.getTotalElements());
            // Thêm các thuộc tính vào ModelAndView
            mv.addObject("customerPage", customerPage);
            mv.addObject("currentPage", page);
            mv.addObject("totalPage", customerPage.getTotalPages());
            mv.addObject("name", name);
            mv.addObject("phone", phone);
            mv.addObject("gender", gender);

            // Kiểm tra nếu không tìm thấy khách hàng
            if (customerPage.getTotalElements() == 0) {
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy khách hàng nào!");
            } else {
                if (page == 0) {
                    session.setAttribute("SUCCESS_MESSAGE", "Tìm thấy " + customerPage.getTotalElements() + " khách hàng!");
                } else {
                    // Nếu trang hiện tại lớn hơn 1
                    session.setAttribute("SUCCESS_MESSAGE", "Trang " + (page + 1) + " của " + customerPage.getTotalElements() + " khách hàng tìm thấy!");
                }
            }
        }
        return mv;
    }


}