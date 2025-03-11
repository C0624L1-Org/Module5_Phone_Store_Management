package com.example.md5_phone_store_management.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Lưu thông báo lỗi vào session
        request.getSession().setAttribute("ERROR_MESSAGE", "Bạn không có quyền truy cập!");

        // Chuyển hướng về trang home
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
