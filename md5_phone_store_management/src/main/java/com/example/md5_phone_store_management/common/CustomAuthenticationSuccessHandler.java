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
                !targetUrl.startsWith("/clear-session") &&
                !targetUrl.contains(".png") &&
                !targetUrl.contains(".jpg") &&
                !targetUrl.contains("/img/")) {
            request.getSession().removeAttribute("REDIRECT_URL");
            return targetUrl;
        }

        targetUrl = null;
        for (var authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            switch (role) {
                case "ROLE_Admin":
                    targetUrl = "/dashboard/admin";
                    break;
                case "ROLE_SalesStaff":
                    targetUrl = "/dashboard/sales-staff";
                    break;
                case "ROLE_SalesPerson":
                    targetUrl = "/dashboard/sales-person";
                    break;
                case "ROLE_WarehouseStaff":
                    targetUrl = "/dashboard/warehouse-staff";
                    break;
                default:
                    break;
            }
            if (targetUrl != null) {
                break;
            }
        }

        if (targetUrl == null) {
            targetUrl = "/";
        }
        return targetUrl;
    }

}
