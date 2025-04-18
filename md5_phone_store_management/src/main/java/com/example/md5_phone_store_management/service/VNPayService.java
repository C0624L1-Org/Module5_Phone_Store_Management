package com.example.md5_phone_store_management.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.md5_phone_store_management.config.VNPayConfig;

@Service
@Transactional
public class VNPayService {
    @Autowired
    private VNPayConfig vnPayConfig;

    public String createPaymentUrl(Long invoiceId, double amount, String orderInfo) throws UnsupportedEncodingException {
        String vnpUrl = vnPayConfig.getVnpUrl();
        String vnpMerchantId = vnPayConfig.getVnpMerchantId();
        String vnpSecretKey = vnPayConfig.getVnpSecretKey();
        String vnpReturnUrl = vnPayConfig.getVnpReturnUrl();

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());

        String vnpTxnRef = String.valueOf(invoiceId);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpMerchantId);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 2);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        List fieldNames = new ArrayList(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnpSecretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        return vnpUrl + "?" + queryUrl;
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac sha512_HMAC = javax.crypto.Mac.getInstance("HmacSHA512");
            sha512_HMAC.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA512"));
            byte[] result = sha512_HMAC.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Xác thực chữ ký từ VNPay
     * @param vnpParams Các tham số từ VNPay
     * @return true nếu chữ ký hợp lệ, false nếu không hợp lệ
     */
    public boolean validateSignature(Map<String, String> vnpParams) {
        // Lấy chữ ký từ VNPay
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            return false;
        }

        // Tạo bản sao của tham số và loại bỏ chữ ký
        Map<String, String> signParams = new HashMap<>(vnpParams);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        // Sắp xếp tham số theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(signParams.keySet());
        Collections.sort(fieldNames);

        // Tạo chuỗi để tính toán chữ ký
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = signParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // Tính toán chữ ký và so sánh
        String secureHash = hmacSHA512(vnPayConfig.getVnpSecretKey(), hashData.toString());
        return secureHash.equals(vnpSecureHash);
    }
}
