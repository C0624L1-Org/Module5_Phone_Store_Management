package com.example.md5_phone_store_management.model.unique;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.md5_phone_store_management.repository.CustomerRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueValidator implements ConstraintValidator<Unique, String> {
    @Autowired
    private CustomerRepository customerRepository;

    private String fieldName;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            if (value == null || value.isBlank()) {
                return true; // Không kiểm tra nếu giá trị rỗng
            }

            // Lấy ID của đối tượng đang được cập nhật (nếu có)
            Integer currentId = getCurrentObjectId();

            // Kiểm tra tính duy nhất dựa trên fieldName
            Long exists = switch (fieldName) {
                case "phone" -> customerRepository.isPhoneExistsExceptId(value, currentId);
                case "email" -> customerRepository.isEmailExistsExceptId(value, currentId);
                default -> 0L;
            };

            return exists == null || exists == 0;
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình validate tính duy nhất: " + e.getMessage());
            e.printStackTrace();
            // Mặc định cho phép validation pass để tránh exception trong validator
            return true;
        }
    }

    // Phương thức để lấy ID của đối tượng đang được cập nhật
    private Integer getCurrentObjectId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String idParam = request.getParameter("customerID");
            return (idParam != null && !idParam.isEmpty()) ? Integer.valueOf(idParam) : null;
        } catch (Exception e) {
            return null;
        }
    }
}