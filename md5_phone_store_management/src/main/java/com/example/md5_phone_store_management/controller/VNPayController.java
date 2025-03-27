package com.example.md5_phone_store_management.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.md5_phone_store_management.model.dto.PaymentResDTO;
import com.example.md5_phone_store_management.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/vnpay")
public class VNPayController {
    @Autowired
    private VNPayService vnPayService;

    // API endpoint for direct integration from JavaScript
    @PostMapping("/create-payment")
    @ResponseBody
    public ResponseEntity<PaymentResDTO> createPayment(@RequestBody Map<String, Object> request) throws UnsupportedEncodingException {
        Long orderId = Long.parseLong(request.get("orderId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());
        String orderInfo = request.get("orderInfo").toString();

        String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, orderInfo);

        PaymentResDTO response = new PaymentResDTO();
        response.setStatus("Ok");
        response.setMessage("Successfully");
        response.setURL(paymentUrl);

        return ResponseEntity.ok(response);
    }

    // Endpoint for direct access from session data
    @GetMapping("/create-direct-payment")
    public String createDirectPayment(HttpSession session) throws UnsupportedEncodingException {
        Long orderId = (Long) session.getAttribute("orderId");
        BigDecimal amount = (BigDecimal) session.getAttribute("totalAmount");
        String orderInfo = "Thanh toan don hang #" + orderId;

        if (orderId == null || amount == null) {
            return "redirect:/dashboard/sales/form?error=missing_payment_data";
        }

        String paymentUrl = vnPayService.createPaymentUrl(orderId, amount.doubleValue(), orderInfo);
        return "redirect:" + paymentUrl;
    }

    // Payment callback endpoint - redirects đến SalesController để xử lý
    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request) {
        return "redirect:/dashboard/sales/payment-callback?" + request.getQueryString();
    }
}
