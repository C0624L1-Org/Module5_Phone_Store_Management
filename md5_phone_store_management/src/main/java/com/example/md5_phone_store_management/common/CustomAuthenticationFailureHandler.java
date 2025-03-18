package com.example.md5_phone_store_management.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Sai tài khoản hoặc mật khẩu!"; // Mặc định

        if (exception instanceof InternalAuthenticationServiceException
                && exception.getCause() instanceof UserDisabledException) {
            errorMessage = exception.getCause().getMessage();
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = exception.getMessage();
        } else if (exception instanceof UserDisabledException) {
            errorMessage = exception.getMessage();
        }

        request.getSession().setAttribute("ERROR_MESSAGE", errorMessage);
        response.sendRedirect("/login");
    }
}
