package com.example.md5_phone_store_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.merchantId}")
    private String vnpMerchantId;

    @Value("${vnpay.secretKey}")
    private String vnpSecretKey;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    public String getVnpUrl() {
        return vnpUrl;
    }

    public String getVnpMerchantId() {
        return vnpMerchantId;
    }

    public String getVnpSecretKey() {
        return vnpSecretKey;
    }

    public String getVnpReturnUrl() {
        return vnpReturnUrl;
    }
}