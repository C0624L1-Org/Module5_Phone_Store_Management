package com.example.md5_phone_store_management.config.security;

import com.example.md5_phone_store_management.common.CustomAccessDeniedHandler;
import com.example.md5_phone_store_management.common.CustomAuthenticationEntryPoint;
import com.example.md5_phone_store_management.common.CustomAuthenticationFailureHandler;
import com.example.md5_phone_store_management.common.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurity {
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        // Chuỗi mã hóa mật khẩu
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/clear-session")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/home",
                                "/",
                                "/register",
                                "/clear-session").permitAll()
                        .requestMatchers("/dashboard/**").hasAnyRole("Admin", "SalesStaff", "SalesPerson", "WarehouseStaff")
                       .requestMatchers("/dashboard/admin/**").hasRole("Admin")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                                .loginPage("/login")
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .loginProcessingUrl("/perform_login")
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler)
                                .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            request.getSession().setAttribute("SUCCESS_MESSAGE", "Đăng xuất thành công!");
                            response.sendRedirect("/login");
                        })
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login")
                        .maximumSessions(1)
                        .expiredUrl("/login")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // Tắt hoàn toàn RequestCache phuc vụ việc redirect về trang cố ý truy cập trước khi đăng nhập
                .requestCache(RequestCacheConfigurer::disable
                );

        return http.build();
    }

}
