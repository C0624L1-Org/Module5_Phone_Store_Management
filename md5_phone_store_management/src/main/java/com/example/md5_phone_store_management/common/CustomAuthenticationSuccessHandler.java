package com.example.md5_phone_store_management.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        request.getSession().setAttribute("SUCCESS_MESSAGE", "Đăng nhập thành công!");

        String redirectUrl = determineTargetUrl(request, authentication);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String determineTargetUrl(HttpServletRequest request, Authentication authentication) {
        // Kiểm tra nếu session có REDIRECT_URL hợp lệ
        String targetUrl = (String) request.getSession().getAttribute("REDIRECT_URL");

        if (targetUrl != null && !targetUrl.isEmpty() &&
                !targetUrl.contains("favicon.ico") &&
                !targetUrl.contains("error") &&
                !targetUrl.contains("dashboard") &&
                !targetUrl.startsWith("/clear-session")) {
            request.getSession().removeAttribute("REDIRECT_URL");
            return targetUrl;
        }

        // Nếu không có, xác định URL dựa trên Role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"))) {
            return "/dashboard/admin";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SalesStaff"))) {
            return "/dashboard/sales";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SalesPerson"))) {
            return "/dashboard/business-staff";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WarehouseStaff"))) {
            return "/dashboard/warehouse-staff";
        } else {
            return "/";
        }
    }
}
