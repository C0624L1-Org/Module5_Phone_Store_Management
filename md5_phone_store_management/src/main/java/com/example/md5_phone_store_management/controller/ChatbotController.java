package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/query")
    public ResponseEntity<String> query(@RequestBody String userInput) {
        try {
            String response = chatbotService.processChatbotRequest(userInput);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Đã có lỗi xảy ra. Vui lòng thử lại!");
        }
    }
}