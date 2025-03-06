package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @GetMapping()
    public ModelAndView getListEmployees(@RequestParam(name = "page",defaultValue = "0",required = false) int page) {
        ModelAndView mv = new ModelAndView("employeesList");
        Pageable pageable =  PageRequest.of(page, 5);
        mv.addObject("currentPage", page);
        mv.addObject("employeePage", employeeService.getAllEmployees(pageable));
        mv.addObject("totalPage",employeeService.getAllEmployees(pageable).getTotalPages());
        return mv;
    }
    @GetMapping("search")
    public ModelAndView searchEmployees(@RequestParam (required = false) String name,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam( required = false) String role,
                                        @RequestParam(name = "page",defaultValue = "0",required = false) int page) {
        ModelAndView mv = new ModelAndView("employeesList");
        Pageable pageable =  PageRequest.of(page, 5);
        mv.addObject("currentPage", page);
        mv.addObject("employeePage", employeeService.searchEmployees(name, phone, role, pageable));
        mv.addObject("totalPage",employeeService.searchEmployees(name, phone, role, pageable).getTotalPages());
        mv.addObject("name",name);
        mv.addObject("phone",phone);
        mv.addObject("role",role);
        return mv;
    }
}
