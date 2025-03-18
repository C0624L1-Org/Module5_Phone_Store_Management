package com.example.md5_phone_store_management.common;

import org.springframework.security.core.AuthenticationException;

public class UserDisabledException extends AuthenticationException {
    public UserDisabledException(String msg) {
        super(msg);
    }
}
