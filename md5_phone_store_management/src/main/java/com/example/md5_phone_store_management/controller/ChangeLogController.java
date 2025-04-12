package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.ChangeLog;
import com.example.md5_phone_store_management.service.implement.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/changelogs")
public class ChangeLogController {

    @Autowired
    private ChangeLogService changeLogService;

    @GetMapping
    public List<ChangeLog> getAllChangeLogs() {
        return changeLogService.getAllChangeLogs();
    }
}