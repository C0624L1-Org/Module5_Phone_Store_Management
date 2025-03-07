package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.implement.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("")
    public String admin(Model model) {
        return "/admin/admin-index";
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        return "/admin/add-employees";
    }

    @PostMapping("/employees/create")
    public String createEmployee(@Valid EmployeeDTO employeeDTO, BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeDTO", employeeDTO);
            return "/admin/add-employees";
        }
        else{
            Employee employee = new Employee();
            BeanUtils.copyProperties(employeeDTO, employee);
            employeeService.addEmployee(employee);
            return "redirect:/admin";
        }

    }
}
