package com.example.md5_phone_store_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @RequestMapping
    public String home() {
        return "home";
    }

    @RequestMapping("/home")
    public String index() {
        return "home";
    }
}
