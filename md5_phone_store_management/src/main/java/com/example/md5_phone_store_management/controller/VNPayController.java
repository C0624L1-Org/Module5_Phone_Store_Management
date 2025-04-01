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

import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.model.dto.PaymentResDTO;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/vnpay")
public class VNPayController {
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private IInvoiceService iInvoiceService;

    // API endpoint for direct integration from JavaScript
    @PostMapping("/create-payment")
    @ResponseBody
    public ResponseEntity<PaymentResDTO> createPayment(@RequestBody Map<String, Object> request) throws UnsupportedEncodingException {
        Long invoiceId = Long.parseLong(request.get("invoiceId").toString());
        double amount = Double.parseDouble(request.get("amount").toString());
        String orderInfo = request.get("orderInfo").toString();

        // Đặt trạng thái của hóa đơn là PROCESSING khi bắt đầu quá trình thanh toán
        try {
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice != null) {
                invoice.setStatus(InvoiceStatus.PROCESSING);
                iInvoiceService.saveInvoice(invoice);
                System.out.println("Invoice " + invoiceId + " set to PROCESSING state");
            }
        } catch (Exception e) {
            System.err.println("Error setting invoice to PROCESSING state: " + e.getMessage());
        }

        String paymentUrl = vnPayService.createPaymentUrl(invoiceId, amount, orderInfo);

        PaymentResDTO response = new PaymentResDTO();
        response.setStatus("Ok");
        response.setMessage("Successfully");
        response.setURL(paymentUrl);

        return ResponseEntity.ok(response);
    }

    // Endpoint for direct access from session data
    @GetMapping("/create-direct-payment")
    public String createDirectPayment(HttpSession session) throws UnsupportedEncodingException {
        // Log thông tin session để debug
        System.out.println("Session attributes: " + session.getAttributeNames().asIterator());
        
        Long invoiceId = (Long) session.getAttribute("invoiceId");
        BigDecimal amount = (BigDecimal) session.getAttribute("totalAmount");
        
        System.out.println("Invoice ID from session: " + invoiceId);
        System.out.println("Amount from session: " + amount);
        
        if (invoiceId == null || amount == null) {
            System.err.println("Missing payment data: invoiceId=" + invoiceId + ", amount=" + amount);
            return "redirect:/dashboard/sales/form?error=missing_payment_data";
        }
        
        String orderInfo = "Thanh toan hoa don #" + invoiceId;
        
        // Đặt trạng thái của hóa đơn là PROCESSING khi bắt đầu quá trình thanh toán
        try {
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice != null) {
                invoice.setStatus(InvoiceStatus.PROCESSING);
                iInvoiceService.saveInvoice(invoice);
                System.out.println("Invoice " + invoiceId + " set to PROCESSING state");
            }
        } catch (Exception e) {
            System.err.println("Error setting invoice to PROCESSING state: " + e.getMessage());
        }
        
        try {
            String paymentUrl = vnPayService.createPaymentUrl(invoiceId, amount.doubleValue(), orderInfo);
            System.out.println("Generated payment URL: " + paymentUrl);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            System.err.println("Error creating payment URL: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard/sales/form?error=payment_url_error";
        }
    }

    // Payment callback endpoint - redirects đến SalesController để xử lý
    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request) {
        return "redirect:/dashboard/sales/payment-callback?" + request.getQueryString();
    }
}
