package com.example.md5_phone_store_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.md5_phone_store_management.service.IEmployeeService;

@Controller
public class LoginController {
    @Autowired
    private IEmployeeService iEmployeeService;

    @RequestMapping("/login")
    public String login() {
        return "auth/login";
    }

//    @GetMapping("/register")
//    public String registerForm(Model model) {
//        model.addAttribute("employeeDTO", new EmployeeDTO());
//        model.addAttribute("roles", Role.values());
//        return "auth/register";
//    }
//
//    @PostMapping("/register")
//    public String registerForm(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
//                               BindingResult bindingResult,
//                               RedirectAttributes redirectAttributes,
//                               Model model) {
//        if (iEmployeeService.existsByUsername(employeeDTO.getUsername())) {
//            bindingResult.rejectValue("username", "", "Tài khoản đã tồn tại!");
//        }
//
//        if (iEmployeeService.existsByEmail(employeeDTO.getEmail())) {
//            bindingResult.rejectValue("email", "", "Email đã tồn tại!");
//        }
//
//        System.out.println("Phone from form: " + employeeDTO.getPhone());
//
//        employeeDTO.validate(employeeDTO, bindingResult);
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("employeeDTO", employeeDTO);
//            return "auth/register";
//        }
//
//        Employee newEmployee = new Employee();
//        BeanUtils.copyProperties(employeeDTO, newEmployee);
//
//        iEmployeeService.addEmployee(newEmployee);
//        redirectAttributes.addFlashAttribute("messageType", "success");
//        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Hãy đăng nhập.");
//
//        return "redirect:/login";
//    }

}
