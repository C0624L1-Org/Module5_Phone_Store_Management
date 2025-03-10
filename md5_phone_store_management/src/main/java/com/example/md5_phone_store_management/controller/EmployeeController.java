package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.dto.EmployeeDTO;
import com.example.md5_phone_store_management.service.IEmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EmployeeController {
    @Autowired
    private IEmployeeService iEmployeeService;

    //Read(a Đình Anh)
    @GetMapping("/employees")
    public ModelAndView getListEmployees(@RequestParam(name = "page",defaultValue = "0",required = false) int page) {
        ModelAndView mv = new ModelAndView("/dashboard/admin/list-employee");
        Pageable pageable =  PageRequest.of(page, 9);
        mv.addObject("currentPage", page);
        mv.addObject("employeePage", iEmployeeService.getAllEmployees(pageable));
        mv.addObject("totalPage",iEmployeeService.getAllEmployees(pageable).getTotalPages());
        return mv;
    }
    @GetMapping("/employees/search")
    public ModelAndView searchEmployees(@RequestParam (required = false) String name,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam( required = false) String role,
                                        @RequestParam(name = "page",defaultValue = "0",required = false) int page) {
        ModelAndView mv = new ModelAndView("/dashboard/admin/list-employee");
        Pageable pageable =  PageRequest.of(page, 9);
        mv.addObject("currentPage", page);
        mv.addObject("employeePage", iEmployeeService.searchEmployees(name, phone, role, pageable));
        mv.addObject("totalPage",iEmployeeService.searchEmployees(name, phone, role, pageable).getTotalPages());
        mv.addObject("name",name);
        mv.addObject("phone",phone);
        mv.addObject("role",role);
        return mv;
    }

    //Create(Tuấn Anh)
    @GetMapping("/employees/create")
    public String employees(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        return "dashboard/admin/create-employee";
    }

    @PostMapping("/employees/create")
    public String createEmployee(@Valid EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeDTO", employeeDTO);
            return "dashboard/admin/create-employee";
        } else {
            Employee employee = new Employee();
            BeanUtils.copyProperties(employeeDTO, employee);
            iEmployeeService.addEmployee(employee);

            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Tạo thành công!");
            return "redirect:/employees";
        }
    }


    // UPDATE(Tân)
    @GetMapping("/admin/employee/edit/{employeeID}")
    public String showUpdateForm(@PathVariable("employeeID") Integer employeeID,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Employee employee = iEmployeeService.getEmployeeById(employeeID);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Người dùng không tồn tại");
            return "redirect:/employees/create";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("roles", Role.values());

        return "dashboard/admin/update-employee";
    }

    @PostMapping("/admin/employee/edit")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Employee employeeToUpdate = iEmployeeService.getEmployeeById(employeeDTO.getEmployeeID());

        if (!employeeToUpdate.getUsername().equals(employeeDTO.getUsername())
                && iEmployeeService.existsByUsername(employeeDTO.getUsername())) {
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", Role.values());
            bindingResult.rejectValue("username", "", "Tài khoản này đã tồn tại!");
        }

        if (!employeeToUpdate.getEmail().equals(employeeDTO.getEmail())
                && iEmployeeService.existsByEmail(employeeDTO.getEmail())) {
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", Role.values());
            bindingResult.rejectValue("email", "", "Email này đã tồn tại!");
        }

        employeeDTO.validate(employeeDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", Role.values());
            return "dashboard/admin/update-employee";
        }

        int updatedRows = iEmployeeService.updateEmployee(
                employeeDTO.getEmployeeID(),
                employeeDTO.getFullName(),
                employeeDTO.getDob(),
                employeeDTO.getAddress(),
                employeeDTO.getPhone(),
                employeeDTO.getRole(),
                employeeDTO.getEmail()
        );

        Employee updatedEmployee = iEmployeeService.getEmployeeById(employeeDTO.getEmployeeID());
        EmployeeDTO updatedEmployeeDTO = new EmployeeDTO();
        BeanUtils.copyProperties(updatedEmployee, updatedEmployeeDTO);
        model.addAttribute("employee", updatedEmployeeDTO);
        model.addAttribute("roles", Role.values());

        redirectAttributes.addFlashAttribute("messageType", "success");
        redirectAttributes.addFlashAttribute("message", "Cập nhật người dùng thành công");

        return "redirect:/employees";
    }

    //delete
    @GetMapping("/employees/delete/{ids}")
    public String deleteEmployee(@PathVariable List<Integer> ids, Model model, RedirectAttributes redirectAttributes) {
        try {
            iEmployeeService.deleteEmployeesById(ids);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Xóa nhân viên thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Xóa nhân viên không thành công");
        }
        return "redirect:/employees";
    }
}
