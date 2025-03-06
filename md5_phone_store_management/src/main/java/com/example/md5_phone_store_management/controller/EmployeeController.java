package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.implement.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EmployeeController {
    @Autowired
    private IEmployeeService iEmployeeService;

    // UPDATE
    @GetMapping("")
    public String showUpdateForm(@PathVariable("employeeID") Integer employeeID,
                                 Model model) {
        Employee employee = iEmployeeService.getEmployeeById(employeeID);

        EmployeeDTO employeeDTO = new EmployeeDTO();
        BeanUtils.copyProperties(employee, employeeDTO);

        model.addAttribute("employee", employeeDTO);
        model.addAttribute("roles", Role.values());

        return "employee/updateForm";
    }

    @PostMapping("")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 Model model) {
        Employee employeeToUpdate = iEmployeeService.getEmployeeById(employeeDTO.getEmployeeID());

        employeeDTO.validate(employeeDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", Role.values());
            return "employee/updateForm";
        }
        return "employee/updateForm";
    }
}
