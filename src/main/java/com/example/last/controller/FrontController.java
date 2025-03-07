package com.example.last.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class FrontController {
    @RequestMapping({"", "/home", "/index"})
    public String home() {
        return "index";
    }
}
