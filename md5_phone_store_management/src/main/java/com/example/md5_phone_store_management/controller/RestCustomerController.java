package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class RestCustomerController {
    @Autowired
    private CustomerService customerService;


    @PostMapping("/admin/customers/update")
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @RequestParam Integer customerID,
            @Valid @ModelAttribute Customer customer,
            BindingResult result,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // Nếu có lỗi validation, trả về danh sách lỗi
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            response.put("status", "error");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }


        Customer customerNeedUpdate = customerService.getCustomerByID(customerID);
        BeanUtils.copyProperties(customer, customerNeedUpdate, "customerID");

        boolean isUpdated = customerService.updateCustomer(customerNeedUpdate);
        if (isUpdated) {
            session.setAttribute("SUCCESS_MESSAGE", "Cập Nhật Khách Hàng Thành Công!");
            response.put("status", "success");
            response.put("redirectUrl", "/dashboard/admin/customers/list"); // Điều hướng khi thành công
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Lỗi trong khi cập nhật khách hàng!");
            session.setAttribute("ERROR_MESSAGE", "Lỗi trong khi Cập Nhật Khách Hàng!");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/admin/customers/create")
    public ResponseEntity<Map<String, Object>> createCustomer(
            @Valid @ModelAttribute Customer customer,
            BindingResult result,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            response.put("status", "error");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        customerService.addNewCustomerAjax(customer);
        session.setAttribute("SUCCESS_MESSAGE", "Thêm khách hàng thành công!");
        response.put("status", "success");
        response.put("redirectUrl", "/dashboard/admin/customers/list");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/customers/edit/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Integer id) {
        Customer customer = customerService.getCustomerByID(id);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Khách hàng không tồn tại."));
        }

        return ResponseEntity.ok(customer);
    }


}
