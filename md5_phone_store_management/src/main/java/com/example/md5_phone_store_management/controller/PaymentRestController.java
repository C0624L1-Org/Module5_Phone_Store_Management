package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.config.vnpay.Config;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.dto.InvoiceResponseDTO;
import com.example.md5_phone_store_management.model.dto.PaymentResDTO;
import com.example.md5_phone_store_management.repository.InvoiceRepository;
import com.example.md5_phone_store_management.service.IInvoiceService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/api/payment")
public class PaymentRestController {

    @Autowired
    private IInvoiceService invoiceService;

    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request) throws UnsupportedEncodingException {
        long amount = 1000000;

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = Config.getIpAddress(request);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", Config.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/api/payment/payment-callback");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Build query
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
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        PaymentResDTO paymentResDTO = new PaymentResDTO();
        paymentResDTO.setStatus("Ok");
        paymentResDTO.setMessage("Successfully");
        paymentResDTO.setURL(paymentUrl);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResDTO);
    }

    // Thêm endpoint để xử lý callback từ VNPAY
    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request,
                                  Model model) {

        // ========= CHỮ KÝ =========
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return "/error/error-page";
        }

        // Tách các tham số và giữ nguyên giá trị đã encoded
        Map<String, String> fields = new HashMap<>();
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            String key = keyValue[0];
            String value = (keyValue.length > 1) ? keyValue[1] : "";
            fields.put(key, value);
        }

        String vnp_SecureHash = fields.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return "/error/error-page";
        }

        // Loại bỏ tham số chữ ký khỏi map để tính toán
        fields.remove("vnp_SecureHash");

        // Tạo chuỗi dữ liệu để hash
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }
            if (i > 0) {
                hashData.append("&");
            }
            hashData.append(fieldName);
            hashData.append("=");
            hashData.append(fieldValue);
        }

        String signValue = Config.hmacSHA512(Config.secretKey, hashData.toString());

        if (!signValue.equals(vnp_SecureHash)) {
            return "/error/error-page";
        }

        // Xử lý kết quả
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");

        // ========= HÓA ĐƠN =========
        if ("00".equals(vnp_ResponseCode)) {
            try {
                // Trích xuất thông tin từ callback
                Invoice invoice = new Invoice();
                invoice.setVnp_TxnRef(fields.get("vnp_TxnRef"));
                invoice.setAmount(Long.parseLong(fields.get("vnp_Amount")) / 100);
                invoice.setOrderInfo(fields.get("vnp_OrderInfo"));
                invoice.setBankCode(fields.get("vnp_BankCode"));
                invoice.setPayDate(fields.get("vnp_PayDate"));
                invoice.setTransactionNo(fields.get("vnp_TransactionNo"));
                invoice.setCardType(fields.get("vnp_CardType"));

                // Lưu vào database
                Invoice savedInvoice = invoiceService.saveInvoice(invoice);

                InvoiceResponseDTO responseDTO = new InvoiceResponseDTO();
                responseDTO.setTransactionId(savedInvoice.getVnp_TxnRef());
                responseDTO.setAmount(savedInvoice.getAmount());
                responseDTO.setOrderInfo(savedInvoice.getOrderInfo());
                responseDTO.setPayDate(savedInvoice.getPayDate());

                // Thêm dữ liệu vào Model
                model.addAttribute("transactionId", savedInvoice.getVnp_TxnRef());
                model.addAttribute("amount", savedInvoice.getAmount());
                model.addAttribute("orderInfo", savedInvoice.getOrderInfo());
                model.addAttribute("bankCode", savedInvoice.getBankCode());
                model.addAttribute("cardType", savedInvoice.getCardType());

                // Xử lý ngày
                try {
                    SimpleDateFormat vnpDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    vnpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                    Date payDate = vnpDateFormat.parse(fields.get("vnp_PayDate"));
                    model.addAttribute("payDate", payDate);
                } catch (ParseException e) {
                    model.addAttribute("payDate", new Date());
                }

                return "/dashboard/sales/payment-invoice";
            } catch (NumberFormatException e) {
                model.addAttribute("error", "Lỗi xử lý dữ liệu: " + e.getMessage());
                return "/error/error-page";
            }
        } else {
            model.addAttribute("error", "Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);
            return "/error/error-page";
        }
    }
}