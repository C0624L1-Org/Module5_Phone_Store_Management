package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.implement.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {


    @Autowired
    private ProductService productService;

    @RequestMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("product", productService.findAllWithOutPageable());
        return "home";
    }
      


}
