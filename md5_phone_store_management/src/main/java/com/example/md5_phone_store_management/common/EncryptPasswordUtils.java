package com.example.md5_phone_store_management.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncryptPasswordUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encryptPasswordUtils(String password) {
        return encoder.encode(password);
    }

    public static Boolean ParseEncrypt(String hashedPassword, String rawPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    public String encode(String newPassword) {
        return encoder.encode(newPassword);
    }
}
