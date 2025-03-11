package com.example.md5_phone_store_management.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionRestController {

    @RequestMapping(value = "/clear-session", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> clearSession(@RequestParam String key, HttpSession session) {
        session.removeAttribute(key);
        return ResponseEntity.ok().build();
    }
}
