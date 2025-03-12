package com.example.md5_phone_store_management.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String requestURI = request.getRequestURI();

        if (requestURI.contains("favicon.ico") ||
                requestURI.contains(".png") ||
                requestURI.contains(".jpg") ||
                requestURI.contains(".jpeg") ||
                requestURI.contains(".gif") ||
                requestURI.contains(".css") ||
                requestURI.contains(".js") ||
                requestURI.startsWith("/img/") ||
                requestURI.startsWith("/css/") ||
                requestURI.startsWith("/js/")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!requestURI.equals("/clear-session")) {
            String targetUrl = requestURI;
            String queryString = request.getQueryString();
            if (queryString != null) {
                targetUrl += "?" + queryString;
            }
            request.getSession().setAttribute("REDIRECT_URL", targetUrl);
        }

        // Redirect tới trang login
        response.sendRedirect("/login");
    }
}
