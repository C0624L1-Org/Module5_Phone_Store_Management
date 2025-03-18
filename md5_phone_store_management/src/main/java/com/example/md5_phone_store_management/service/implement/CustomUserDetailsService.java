package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.common.UserDisabledException;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.repository.IEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private IEmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Employee> optionalEmployee = employeeRepository.findByUsername(username);
        Employee employee = optionalEmployee.orElseThrow(() ->
                new UsernameNotFoundException("Sai tài khoản hoặc mật khẩu")
        );

        return User.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .roles(employee.getRole().name())
                .build();
    }
}
