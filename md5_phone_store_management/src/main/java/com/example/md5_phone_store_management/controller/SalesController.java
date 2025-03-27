package com.example.md5_phone_store_management.controller;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Gender;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.PDFExportService;
import com.example.md5_phone_store_management.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/dashboard/sales")
@Controller
public class SalesController {

    @Autowired
    private ICustomerService iCustomerService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IInvoiceService iInvoiceService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PDFExportService pdfExportService;

    @GetMapping("/form")
    public String openSalesForm(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                @RequestParam(name = "success", required = false) Boolean success,
                                @RequestParam(name = "error", required = false) String error,
                                Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Customer> customerList = iCustomerService.findAllCustomers(pageable);
        Page<Product> productList = iProductService.findAll(pageable);

        model.addAttribute("customerList", customerList);
        model.addAttribute("productList", productList);
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("invoiceDetail", new InvoiceDetail());

        if (Boolean.TRUE.equals(success)) {
            model.addAttribute("messageType", "success");
            model.addAttribute("message", "Thanh toán thành công!");
        } else if (error != null) {
            model.addAttribute("messageType", "error");
            switch (error) {
                case "payment_failed":
                    model.addAttribute("message", "Thanh toán thất bại!");
                    break;
                case "invoice_not_found":
                    model.addAttribute("message", "Không tìm thấy hóa đơn!");
                    break;
                case "invalid_payment_data":
                    model.addAttribute("message", "Dữ liệu thanh toán không hợp lệ!");
                    break;
                default:
                    model.addAttribute("message", "Đã xảy ra lỗi: " + error);
            }
        }

        return "dashboard/sales/form";
    }

    @GetMapping("/search-customers")
    @ResponseBody
    public ResponseEntity<Page<Customer>> searchCustomers(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "type", required = false, defaultValue = "name") String type,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Customer> customers;

        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("name".equals(type)) {
                customers = iCustomerService.searchCustomers(keyword, null, null, pageable);
            } else if ("phone".equals(type)) {
                customers = iCustomerService.searchCustomers(null, keyword, null, pageable);
            } else {
                // Mặc định tìm theo tên
                customers = iCustomerService.searchCustomers(keyword, null, null, pageable);
            }
        } else {
            customers = iCustomerService.findAllCustomers(pageable);
        }

        return ResponseEntity.ok(customers);
    }

    /**
     * API endpoint để tìm kiếm khách hàng theo tên, email hoặc số điện thoại
     */
    @GetMapping("/customers/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchCustomersAdvanced(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Customer> customers;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Tìm kiếm kết hợp theo tên, số điện thoại hoặc email
                String searchTerm = keyword.trim();
                
                // Tìm kiếm trong repository
                customers = iCustomerService.searchCustomersByNameOrPhoneOrEmail(searchTerm, searchTerm, searchTerm, pageable);
            } else {
                customers = iCustomerService.findAllCustomers(pageable);
            }
            
            // Chuyển đổi dữ liệu sang format phù hợp với frontend
            List<Map<String, Object>> customerList = customers.getContent().stream()
                .map(customer -> {
                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("id", customer.getCustomerID());
                    customerMap.put("fullName", customer.getFullName());
                    customerMap.put("phone", customer.getPhone());
                    customerMap.put("email", customer.getEmail());
                    customerMap.put("address", customer.getAddress());
                    customerMap.put("purchaseCount", customer.getPurchaseCount());
                    return customerMap;
                })
                .collect(Collectors.toList());
            
            response.put("status", "success");
            response.put("customers", customerList);
            response.put("currentPage", page);
            response.put("totalItems", customers.getTotalElements());
            response.put("totalPages", customers.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tìm kiếm khách hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * API để tìm kiếm khách hàng theo số điện thoại
     */
    @GetMapping("/customers/search-by-phone")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchCustomerByPhone(@RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (phone == null || phone.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Số điện thoại không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Tìm khách hàng theo số điện thoại
            Pageable pageable = PageRequest.of(0, 1);
            Page<Customer> customerPage = iCustomerService.searchCustomers(null, phone, null, pageable);
            
            if (!customerPage.isEmpty()) {
                Customer customer = customerPage.getContent().get(0);
                Map<String, Object> customerData = new HashMap<>();
                customerData.put("id", customer.getCustomerID());
                customerData.put("fullName", customer.getFullName());
                customerData.put("phone", customer.getPhone());
                customerData.put("email", customer.getEmail());
                customerData.put("address", customer.getAddress());
                customerData.put("purchaseCount", customer.getPurchaseCount());
                
                response.put("status", "success");
                response.put("customer", customerData);
            } else {
                response.put("status", "not_found");
                response.put("message", "Không tìm thấy khách hàng với số điện thoại: " + phone);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tìm kiếm khách hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/search-products")
    @ResponseBody
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = iProductService.searchProductByNameAndSupplier_NameAndPurchasePrice(keyword, null, 0, pageable);
        } else {
            products = iProductService.findAll(pageable);
        }

        return ResponseEntity.ok(products);
    }

    @PostMapping("/add")
    public String processPayment(@ModelAttribute("invoice") Invoice invoice,
                                 @RequestParam("productID") List<Integer> productIDs,
                                 @RequestParam("quantity") List<Integer> quantities,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam(value = "printInvoice", required = false) Boolean printInvoice,
                                 HttpSession session,
                                 Model model) {

        // Kiểm tra khách hàng mới hay cũ
        Customer customer;
        if (invoice.getCustomer().getCustomerID() != null) {
            customer = iCustomerService.findCustomerById(invoice.getCustomer().getCustomerID());
            if (customer == null) {
                model.addAttribute("messageType", "error");
                model.addAttribute("message", "Không tìm thấy khách hàng!");
                return "redirect:/dashboard/sales/form";
            }
            invoice.setCustomer(customer);
        } else {
            // Tạo khách hàng mới
            customer = new Customer();
            customer.setFullName(invoice.getCustomer().getFullName());
            customer.setEmail(invoice.getCustomer().getEmail());
            customer.setPhone(invoice.getCustomer().getPhone());
            customer.setAddress(invoice.getCustomer().getAddress());
            customer.setGender(Gender.Other);
            customer.setPurchaseCount(0);
            try {
                customer = iCustomerService.saveCustomer(customer);
                invoice.setCustomer(customer);
            } catch (Exception e) {
                model.addAttribute("messageType", "error");
                model.addAttribute("message", "Email hoặc SĐT đã tồn tại");
                return "redirect:/dashboard/sales/form";
            }
        }

        // Tạo chi tiết hóa đơn từ sản phẩm và số lượng
        List<InvoiceDetail> invoiceDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Kiểm tra số lượng sản phẩm
        boolean stockError = false;
        String errorMessage = "";

        for (int i = 0; i < productIDs.size(); i++) {
            if (productIDs.get(i) != null && quantities.get(i) != null && quantities.get(i) > 0) {
                Product product = iProductService.getProductById(productIDs.get(i));
                if (product != null) {
                    // Kiểm tra số lượng tồn kho
                    if (product.getStockQuantity() < quantities.get(i)) {
                        stockError = true;
                        errorMessage += "Sản phẩm " + product.getName() + " chỉ còn " + product.getStockQuantity() + " sản phẩm. ";
                        continue;
                    }

                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setProduct(product);
                    detail.setQuantity(quantities.get(i));

                    // Tính tổng giá cho mỗi sản phẩm
                    BigDecimal productTotal = product.getSellingPrice().multiply(BigDecimal.valueOf(quantities.get(i)));
                    detail.setTotalPrice(productTotal);

                    // Cộng vào tổng hóa đơn
                    totalAmount = totalAmount.add(productTotal);

                    invoice.addInvoiceDetail(detail);
                }
            }
        }

        if (stockError) {
            model.addAttribute("messageType", "error");
            model.addAttribute("message", "Lỗi số lượng: " + errorMessage);
            return "redirect:/dashboard/sales/form";
        }

        if (invoice.getInvoiceDetailList() == null || invoice.getInvoiceDetailList().isEmpty()) {
            model.addAttribute("messageType", "error");
            model.addAttribute("message", "Vui lòng chọn ít nhất một sản phẩm!");
            return "redirect:/dashboard/sales/form";
        }

        // Lưu thông tin hóa đơn
        invoice.setAmount(totalAmount.longValue());
        invoice.setOrderInfo("Thanh toán đơn hàng");

        // Lưu hóa đơn vào DB
        invoice = iInvoiceService.saveInvoice(invoice);

        // Lưu ID hóa đơn vào session để xử lý sau khi thanh toán
        session.setAttribute("invoiceId", invoice.getId());
        session.setAttribute("printInvoice", printInvoice);

        // Xử lý theo phương thức thanh toán
        if ("cash".equals(paymentMethod)) {
            // Thanh toán tiền mặt - xử lý ngay
            return processSuccessfulPayment(invoice.getId(), Boolean.TRUE.equals(printInvoice));
        } else if ("vnpay".equals(paymentMethod)) {
            // Thanh toán VNPay - chuyển hướng đến cổng thanh toán
            try {
                // Lưu thông tin vào session để sử dụng sau khi thanh toán
                session.setAttribute("totalAmount", totalAmount);

                // Tạo URL thanh toán VNPay
                String paymentUrl = vnPayService.createPaymentUrl(
                        invoice.getId(),
                        totalAmount.doubleValue(),
                        "Thanh toan don hang #" + invoice.getId()
                );

                // Chuyển hướng đến trang thanh toán VNPay
                return "redirect:" + paymentUrl;
            } catch (UnsupportedEncodingException e) {
                model.addAttribute("messageType", "error");
                model.addAttribute("message", "Lỗi khi tạo URL thanh toán: " + e.getMessage());
                return "redirect:/dashboard/sales/form";
            }
        }

        return "redirect:/dashboard/sales/form";
    }

    /**
     * Xuất hóa đơn sang PDF và tải xuống
     */
    @GetMapping("/download-invoice-pdf/{invoiceId}")
    public ResponseEntity<InputStreamResource> downloadInvoicePdf(@PathVariable Long invoiceId) {
        try {
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice == null) {
                return ResponseEntity.notFound().build();
            }

            // Lấy thông tin ngày hiện tại để hiển thị trên hóa đơn
            String payDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            // Nếu hóa đơn có chứa thông tin thanh toán VNPay, sử dụng nó
            if (invoice.getPayDate() != null && !invoice.getPayDate().isEmpty()) {
                payDate = invoice.getPayDate();
            }

            // Xác định phương thức thanh toán
            String bankCode = invoice.getBankCode() != null ? invoice.getBankCode() : "CASH";
            String cardType = invoice.getCardType() != null ? invoice.getCardType() : "CASH";

            // Tạo PDF
            ByteArrayInputStream pdfStream = pdfExportService.generateInvoicePdf(invoice, payDate, bankCode, cardType);

            // Thiết lập HTTP header
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=invoice-" + invoice.getId() + ".pdf");

            // Trả về response với PDF
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Xử lý thanh toán thành công
     */
    private String processSuccessfulPayment(Long invoiceId, boolean printInvoice) {
        try {
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice == null) {
                System.err.println("Không tìm thấy hoá đơn với ID = " + invoiceId);
                return "redirect:/dashboard/sales/form?error=invoice_not_found";
            }

            System.out.println("Xử lý thanh toán thành công cho hóa đơn: " + invoice);

            boolean hasError = false;
            String errorMessage = "";

            try {
                // Cập nhật số lần mua hàng của khách hàng
                try {
                    updateCustomerPurchaseCount(invoice);
                } catch (Exception e) {
                    System.err.println("Không cập nhật được số lượng mua hàng của khách hàng, nhưng sẽ tiếp tục: " + e.getMessage());
                    hasError = true;
                    errorMessage += "customer_update_error;";
                }

                // Cập nhật số lượng sản phẩm trong kho
                try {
                    updateProductStock(invoice);
                } catch (Exception e) {
                    System.err.println("Không cập nhật được kho sản phẩm nhưng sẽ tiếp tục: " + e.getMessage());
                    hasError = true;
                    errorMessage += "product_stock_update_error;";
                }

                // Hiển thị chi tiết hóa đơn
                return "redirect:/dashboard/sales/invoice-pdf/" + invoiceId;
            } catch (Exception e) {
                System.err.println("Error processing successful payment: " + e.getMessage());
                e.printStackTrace();
                return "redirect:/dashboard/sales/form?error=processing_error";
            }
        } catch (Exception e) {
            System.err.println("Error in processSuccessfulPayment: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard/sales/form?error=general_processing_error";
        }
    }

    /**
     * Cập nhật số lần mua hàng của khách hàng
     */
    private void updateCustomerPurchaseCount(Invoice invoice) {
        try {
            Customer customer = invoice.getCustomer();
            if (customer != null) {
                Customer freshCustomer = iCustomerService.findCustomerById(customer.getCustomerID());
                if (freshCustomer != null) {
                    int newCount = freshCustomer.getPurchaseCount() + 1;
                    System.out.println("Đang cập nhật số lượng mua hàng cho khách hàng ID = " + freshCustomer.getCustomerID() +
                            " từ " + freshCustomer.getPurchaseCount() + " thành " + newCount);

                    try {
                        Integer customerId = freshCustomer.getCustomerID();
                        iCustomerService.updatePurchaseCount(customerId, newCount);
                        System.out.println("Đã cập nhật thành công số lượng mua hàng cho khách hàng: " + freshCustomer.getFullName() +
                                ", số lần mua mới: " + newCount);
                    } catch (Exception e) {
                        System.err.println("Không cập nhật được số lượng mua hàng: " + e.getMessage());
                        e.printStackTrace();
                        throw e;
                    }
                } else {
                    System.err.println("Không tìm thấy khách hàng mới có ID: " + customer.getCustomerID());
                }
            } else {
                System.err.println("Khách hàng là null cho ID hóa đơn: " + invoice.getId());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật số lượng mua hàng của khách hàng: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong kho
     */
    private void updateProductStock(Invoice invoice) {
        try {
            if (invoice.getInvoiceDetailList() != null) {
                for (InvoiceDetail detail : invoice.getInvoiceDetailList()) {
                    if (detail.getProduct() != null) {
                        Product freshProduct = iProductService.getProductById(detail.getProduct().getProductID());
                        if (freshProduct != null) {
                            int newQuantity = freshProduct.getStockQuantity() - detail.getQuantity();
                            if (newQuantity < 0) {
                                System.err.println("CẢNH BÁO: Sản phẩm " + freshProduct.getName() + " bị âm");
                                newQuantity = 0; // Đảm bảo không có số lượng âm
                            }

                            try {
                                iProductService.updateStockQuantity(freshProduct.getProductID(), newQuantity);
                                System.out.println("Cập nhật số lượng của sản phẩm: " + freshProduct.getName() +
                                        ", số lượng mới: " + newQuantity);
                            } catch (Exception e) {
                                System.err.println("Error updating stock quantity with direct query: " + e.getMessage());
                                e.printStackTrace();
                                throw e;
                            }
                        } else {
                            System.err.println("Lỗi khi cập nhật số lượng hàng của ID = " + detail.getProduct().getProductID());
                        }
                    } else {
                        System.err.println("Sản phẩm là null của hoá đơn ID = " + invoice.getId());
                    }
                }
            } else {
                System.err.println("Danh sách chi tiết hóa đơn là null ID = " + invoice.getId());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật kho sản phẩm: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/invoice-pdf/{invoiceId}")
    public String generateInvoicePdf(@PathVariable Long invoiceId, Model model) {
        Invoice invoice = iInvoiceService.findById(invoiceId);
        if (invoice == null) {
            return "redirect:/dashboard/sales/form?error=invoice_not_found";
        }

        model.addAttribute("invoice", invoice);
        model.addAttribute("transactionId", invoice.getId());
        model.addAttribute("amount", invoice.getAmount());
        model.addAttribute("orderInfo", invoice.getOrderInfo());

        // Sử dụng thông tin từ hóa đơn nếu có, không thì mới dùng giá trị mặc định
        Date paymentDate;
        if (invoice.getPayDate() != null && !invoice.getPayDate().isEmpty()) {
            try {
                // Thử chuyển đổi chuỗi payDate từ hóa đơn thành đối tượng Date
                paymentDate = new java.text.SimpleDateFormat("yyyyMMddHHmmss").parse(invoice.getPayDate());
            } catch (Exception e) {
                // Nếu có lỗi chuyển đổi, dùng thời gian hiện tại
                paymentDate = new Date();
            }
        } else {
            paymentDate = new Date();
        }
        model.addAttribute("payDate", paymentDate);

        // Sử dụng mã ngân hàng từ hóa đơn
        String bankCode = invoice.getBankCode() != null && !invoice.getBankCode().isEmpty() ?
                invoice.getBankCode() : "CASH";
        model.addAttribute("bankCode", bankCode);

        // Sử dụng loại thẻ từ hóa đơn
        String cardType = invoice.getCardType() != null && !invoice.getCardType().isEmpty() ?
                invoice.getCardType() : "CASH";
        model.addAttribute("cardType", cardType);

        return "dashboard/sales/payment-invoice";
    }

    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request, HttpSession session, Model model) {
        try {
            // Lấy các tham số từ VNPay trả về
            Map<String, String> vnpParams = new HashMap<>();
            request.getParameterMap().forEach((key, value) -> {
                if (value != null && value.length > 0) {
                    vnpParams.put(key, value[0]);
                }
            });

            System.out.println("VNPay callback parameters:");
            vnpParams.forEach((key, value) -> System.out.println(key + ": " + value));

            // Xác thực chữ ký từ VNPay
            if (!vnPayService.validateSignature(vnpParams)) {
                System.err.println("Invalid VNPay signature");
                return "redirect:/dashboard/sales/form?error=invalid_signature";
            }

            String vnpResponseCode = vnpParams.get("vnp_ResponseCode");

            // Lấy ID hóa đơn từ tham số VNPay trước tiên
            Long invoiceId = null;
            String txnRef = vnpParams.get("vnp_TxnRef");
            if (txnRef != null) {
                try {
                    invoiceId = Long.parseLong(txnRef);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid txnRef format: " + txnRef);
                    return "redirect:/dashboard/sales/form?error=invalid_txnref_format";
                }
            }

            // Nếu không tìm thấy trong tham số, thử lấy từ session
            if (invoiceId == null) {
                invoiceId = (Long) session.getAttribute("invoiceId");
                if (invoiceId == null) {
                    return "redirect:/dashboard/sales/form?error=missing_invoice_id";
                }
            }

            Boolean printInvoice = (Boolean) session.getAttribute("printInvoice");

            // Tìm hóa đơn trước khi xử lý
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice == null) {
                return "redirect:/dashboard/sales/form?error=invoice_not_found";
            }

            // Kiểm tra kết quả thanh toán
            if ("00".equals(vnpResponseCode)) {
                // Thanh toán thành công - Cập nhật thông tin hóa đơn với dữ liệu từ VNPay
                try {
                    System.out.println("Hóa đơn trước khi cập nhật: " + invoice);

                    // Cập nhật thông tin từ VNPay
                    invoice.setVnp_TxnRef(vnpParams.get("vnp_TxnRef"));

                    // Không cập nhật số tiền từ VNPay, giữ nguyên số tiền ban đầu
                    // Vì số tiền từ VNPay có thể quá lớn và gây lỗi

                    // Cập nhật các thông tin khác từ VNPay
                    String orderInfo = vnpParams.get("vnp_OrderInfo");
                    if (orderInfo != null && !orderInfo.isEmpty()) {
                        invoice.setOrderInfo(orderInfo);
                    }

                    String bankCode = vnpParams.get("vnp_BankCode");
                    if (bankCode != null && !bankCode.isEmpty()) {
                        invoice.setBankCode(bankCode);
                    }

                    String payDate = vnpParams.get("vnp_PayDate");
                    if (payDate != null && !payDate.isEmpty()) {
                        invoice.setPayDate(payDate);
                    }

                    String transactionNo = vnpParams.get("vnp_TransactionNo");
                    if (transactionNo != null && !transactionNo.isEmpty()) {
                        invoice.setTransactionNo(transactionNo);
                    }

                    String cardType = vnpParams.get("vnp_CardType");
                    if (cardType != null && !cardType.isEmpty()) {
                        invoice.setCardType(cardType);
                    }

                    // Log thông tin hóa đơn sau khi cập nhật
                    System.out.println("Hoá đơn sau khi cập nhật: " + invoice);

                    try {
                        // Lưu hóa đơn đã cập nhật
                        invoice = iInvoiceService.saveInvoice(invoice);

                        // Xử lý thanh toán thành công (cập nhật số lượng sản phẩm với số lần mua hàng)
                        return processSuccessfulPayment(invoiceId, Boolean.TRUE.equals(printInvoice));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi lưu hóa đơn đã cập nhật: " + e.getMessage());
                        e.printStackTrace();

                        // Nếu lỗi xảy ra khi lưu hóa đơn, thử xử lý thanh toán mà không cập nhật thông tin từ VNPAY
                        System.out.println("Đang cố gắng xử lý thanh toán mà không cần cập nhật thông tin từ VNPAY");
                        return processSuccessfulPayment(invoiceId, Boolean.TRUE.equals(printInvoice));
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi cập nhật hóa đơn với dữ liệu VNPay: " + e.getMessage());
                    e.printStackTrace();
                    return "redirect:/dashboard/sales/form?error=update_invoice_error";
                }
            } else {
                // Thanh toán thất bại - không cập nhật số lượng sản phẩm và số lần mua hàng
                return "redirect:/dashboard/sales/form?error=payment_failed&code=" + vnpResponseCode;
            }
        } catch (Exception e) {
            System.err.println("Error processing VNPay callback: " + e.getMessage());
            e.printStackTrace();

            // Trả về thông báo lỗi chung
            return "redirect:/dashboard/sales/form?error=payment_error";
        }
    }
}
