package com.example.md5_phone_store_management.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.service.CustomerService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("")
public class RestCustomerController {
    @Autowired
    private CustomerService customerService;


    @PostMapping("/dashboard/admin/customers/update")
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


    @PostMapping("/dashboard/admin/customers/create")
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

    /**
     * API endpoint để tạo khách hàng mới từ trang bán hàng
     * Yêu cầu các trường: fullName, phone, email, dob, address, gender, purchaseCount
     */
    @PostMapping("/api/create-customer")
    public ResponseEntity<Map<String, Object>> apiCreateCustomer(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String dob,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int purchaseCount,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validation cơ bản
            if (fullName == null || fullName.trim().isEmpty()) {
                session.setAttribute("ERROR_MESSAGE", "Họ và tên không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            if (phone == null || phone.trim().isEmpty()) {
                session.setAttribute("ERROR_MESSAGE", "Số điện thoại không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra định dạng số điện thoại
            if (!phone.matches("\\d{10,15}")) {
                session.setAttribute("ERROR_MESSAGE", "Số điện thoại phải chứa từ 10 đến 15 chữ số");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra email nếu có
            if (email != null && !email.trim().isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                session.setAttribute("ERROR_MESSAGE", "Định dạng email không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo đối tượng khách hàng mới
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhone(phone);
            customer.setEmail(email);
            customer.setAddress(address);

            // Xử lý ngày sinh nếu có
            if (dob != null && !dob.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date birthDate = dateFormat.parse(dob);

                    // Kiểm tra ngày sinh trong quá khứ
                    if (birthDate.after(new Date())) {
                        session.setAttribute("ERROR_MESSAGE", "Ngày sinh phải là ngày trong quá khứ");
                        return ResponseEntity.badRequest().body(response);
                    }

                    customer.setDob(birthDate);
                } catch (ParseException e) {
                    session.setAttribute("ERROR_MESSAGE", "Định dạng ngày sinh không hợp lệ");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Xử lý giới tính
            if (gender != null && !gender.isEmpty()) {
                try {
                    customer.setGender(Gender.valueOf(gender));
                } catch (IllegalArgumentException e) {
                    session.setAttribute("ERROR_MESSAGE", "Giá trị giới tính không hợp lệ (Male, Female, hoặc Other");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                // Mặc định là Other nếu không cung cấp
                customer.setGender(Gender.Other);
            }

            // Thiết lập số lần mua hàng
            customer.setPurchaseCount(purchaseCount);

            // Lưu khách hàng mới
            Customer savedCustomer = customerService.addNewCustomerAjax(customer);

            if (savedCustomer != null && savedCustomer.getCustomerID() != null) {
                session.setAttribute("SUCCESS_MESSAGE", "Đăng ký khách hàng mới thành công");
                response.put("customerId", savedCustomer.getCustomerID());
                return ResponseEntity.ok(response);
            } else {
                session.setAttribute("ERROR_MESSAGE", "Không thể lưu thông tin khách hàng");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("ERROR_MESSAGE", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
