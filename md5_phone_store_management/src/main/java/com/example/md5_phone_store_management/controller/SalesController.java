
package com.example.md5_phone_store_management.controller;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.md5_phone_store_management.service.implement.InvoiceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.InvoiceDetail;
import com.example.md5_phone_store_management.model.InvoiceStatus;
import com.example.md5_phone_store_management.model.PaymentMethod;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.service.ICustomerService;
import com.example.md5_phone_store_management.service.IEmployeeService;
import com.example.md5_phone_store_management.service.IInvoiceService;
import com.example.md5_phone_store_management.service.IProductService;
import com.example.md5_phone_store_management.service.PDFExportService;
import com.example.md5_phone_store_management.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard/sales")
public class SalesController {

    @Autowired
    private ICustomerService iCustomerService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IInvoiceService iInvoiceService;

    @Autowired
    private InvoiceServiceImpl invoiceServiceImpl;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PDFExportService pdfExportService;

    @Autowired
    private IEmployeeService iEmployeeService;

    @GetMapping("/form")
    public String openSalesForm(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                @RequestParam(name = "success", required = false) Boolean success,
                                @RequestParam(name = "error", required = false) String error,
                                Model model,
                                HttpSession session) {
        Pageable pageable = PageRequest.of(page, 5);
        // Tất cả khách hàng
        Page<Customer> customerList = iCustomerService.findAllCustomers(pageable);
        // Khách hàng có purchaseCount > 0
        Page<Customer> customerListWithPurchaseCount = iCustomerService.findCustomersWithPurchases(pageable);
        // Tất cả sản phẩm
        Page<Product> productList = iProductService.findAll(pageable);

        System.out.println("Số lượng khách hàng: " + customerList.getTotalElements());
        System.out.println("Số lượng sản phẩm: " + productList.getTotalElements());

        model.addAttribute("customerList", customerList);
        model.addAttribute("customerListWithPurchaseCount", customerListWithPurchaseCount);
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

        // Thêm vào model để có thể render toast nếu cần
        if (session.getAttribute("SUCCESS_MESSAGE") != null) {
            model.addAttribute("messageType", "success");
            model.addAttribute("message", session.getAttribute("SUCCESS_MESSAGE"));
            session.removeAttribute("SUCCESS_MESSAGE");
        } else if (session.getAttribute("ERROR_MESSAGE") != null) {
            model.addAttribute("messageType", "error");
            model.addAttribute("message", session.getAttribute("ERROR_MESSAGE"));
            session.removeAttribute("ERROR_MESSAGE");
        }

        return "dashboard/sales/form";
    }


@PostMapping("/add")
public String processPayment(@ModelAttribute("invoice") Invoice invoice,
                             @RequestParam("productID") List<Integer> productIDs,
                             @RequestParam("quantity") List<Integer> quantities,
                             @RequestParam("paymentMethod") String paymentMethodStr,
                             @RequestParam(value = "printInvoice", required = false) Boolean printInvoice,
                             HttpSession session,
                             Model model) {
    try {
        System.out.println("DEBUG: Đã nhận request thanh toán");
        System.out.println("DEBUG: Invoice: " + invoice);
        System.out.println("DEBUG: Customer ID: " + (invoice.getCustomer() != null ? invoice.getCustomer().getCustomerID() : "null"));
        System.out.println("DEBUG: ProductIDs: " + productIDs);
        System.out.println("DEBUG: Quantities: " + quantities);
        System.out.println("DEBUG: Payment Method: " + paymentMethodStr);
        System.out.println("DEBUG: Print Invoice: " + printInvoice + " (type: " + (printInvoice != null ? printInvoice.getClass().getName() : "null") + ")");

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG: Payment method not valid: " + e.getMessage());
            paymentMethod = PaymentMethod.CASH;
        }

        invoice.setStatus(InvoiceStatus.PROCESSING);
        invoice.setPaymentMethod(paymentMethod);

        // Gán nhân viên đang đăng nhập
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                System.out.println("Current authenticated user: " + username);
                Optional<Employee> employeeOpt = iEmployeeService.findByUsername(username);
                if (employeeOpt.isPresent()) {
                    invoice.setEmployee(employeeOpt.get());
                    System.out.println("Assigned employee: " + employeeOpt.get().getFullName());
                } else {
                    System.err.println("Employee not found for username: " + username);
                }
            } else {
                System.err.println("No authentication information found");
            }
        } catch (Exception e) {
            System.err.println("Error assigning employee: " + e.getMessage());
            e.printStackTrace();
        }

        // Kiểm tra danh sách
        if (productIDs == null || quantities == null || productIDs.size() != quantities.size()) {
            session.setAttribute("ERROR_MESSAGE", "Danh sách sản phẩm không hợp lệ!");
            return "redirect:/dashboard/sales/form?error=invalid_data";
        }

        // Kiểm tra customer
        if (invoice.getCustomer() == null || invoice.getCustomer().getCustomerID() == null) {
            session.setAttribute("ERROR_MESSAGE", "Khách hàng không được để trống!");
            return "redirect:/dashboard/sales/form?error=missing_customer";
        }

        Customer customer = iCustomerService.findCustomerById(invoice.getCustomer().getCustomerID());
        if (customer == null) {
            session.setAttribute("ERROR_MESSAGE", "Khách hàng không tồn tại!");
            return "redirect:/dashboard/sales/form?error=customer_not_found";
        }
        invoice.setCustomer(customer);

        // Tạo danh sách chi tiết hóa đơn
        List<InvoiceDetail> invoiceDetails = new ArrayList<>();
        Long totalAmount = 0L;

        for (int i = 0; i < productIDs.size(); i++) {
            Integer productId = productIDs.get(i);
            Integer quantity = quantities.get(i);

            if (productId == null || quantity == null || quantity <= 0) {
                continue;
            }

            Product product = iProductService.getProductById(productId);
            if (product == null) {
                continue;
            }

            if (quantity > product.getStockQuantity()) {
                session.setAttribute("ERROR_MESSAGE", "Số lượng sản phẩm " + product.getName() + " không đủ!");
                return "redirect:/dashboard/sales/form?error=insufficient_stock&product=" + product.getName() +
                        "&available=" + product.getStockQuantity() + "&requested=" + quantity;
            }

            InvoiceDetail detail = new InvoiceDetail();
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setTotalPrice(product.getRetailPrice().multiply(BigDecimal.valueOf(quantity)));

            invoiceDetails.add(detail);
            totalAmount += product.getRetailPrice().multiply(BigDecimal.valueOf(quantity)).longValue();
        }

        if (invoiceDetails.isEmpty()) {
            session.setAttribute("ERROR_MESSAGE", "Không có sản phẩm nào được chọn!");
            return "redirect:/dashboard/sales/form?error=no_products";
        }

        invoice.setInvoiceDetailList(invoiceDetails);
        invoice.setAmount(totalAmount);

        // Thiết lập orderInfo trước khi lưu
        String orderInfo = "Thanh toán hóa đơn";
        invoice.setOrderInfo(orderInfo);

        // Lưu hóa đơn
        System.out.println("DEBUG: Saving invoice first time");
        invoice = iInvoiceService.saveInvoice(invoice);

        if (invoice == null || invoice.getId() == null) {
            session.setAttribute("ERROR_MESSAGE", "Lỗi khi lưu hóa đơn!");
            return "redirect:/dashboard/sales/form?error=save_failed";
        }

        // Cập nhật orderInfo với mã hóa đơn
        invoice.setOrderInfo("Thanh toán đơn hàng #" + invoice.getId());

        // Lưu ID hóa đơn và flag in hóa đơn vào session
        session.setAttribute("invoiceId", invoice.getId());
        session.setAttribute("orderId", invoice.getId());
        session.setAttribute("totalAmount", BigDecimal.valueOf(totalAmount));
        session.setAttribute("printInvoice", printInvoice);

        // Xử lý theo phương thức thanh toán
        if (paymentMethod == PaymentMethod.VNPAY) {
            return "redirect:/api/vnpay/create-direct-payment";
        } else {
            return processSuccessfulPayment(invoice.getId(), Boolean.TRUE.equals(printInvoice), session);
        }
    } catch (Exception e) {
        e.printStackTrace();
        session.setAttribute("ERROR_MESSAGE", "Lỗi khi xử lý thanh toán: " + e.getMessage());
        return "redirect:/dashboard/sales/form?error=system_error";
    }
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

            // Sử dụng PaymentMethod từ hóa đơn
            ByteArrayInputStream pdfStream = pdfExportService.generateInvoicePdf(invoice, payDate);

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



    // từ hậu
    private String processSuccessfulPayment(Long invoiceId, boolean printInvoice, HttpSession session) {
        try {
            System.out.println("DEBUG: Processing successful payment for invoice ID: " + invoiceId);
            System.out.println("DEBUG: Print Invoice flag value: " + printInvoice + " (primitive boolean)");

            // Lấy thông tin hóa đơn
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice == null) {
                System.err.println("ERROR: Invoice not found with ID: " + invoiceId);
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy hóa đơn!");
                return "redirect:/dashboard/sales/form?error=invoice_not_found";
            }

            System.out.println("DEBUG: Found invoice: " + invoice);

            // Kiểm tra trạng thái đơn hàng thành công
            invoice.setStatus(InvoiceStatus.SUCCESS);

            // Thiết lập ngày thanh toán cho phương thức tiền mặt
            if (invoice.getPaymentMethod() == PaymentMethod.CASH) {
                // Định dạng ngày thanh toán theo cùng định dạng với VNPAY (yyyyMMddHHmmss)
                String payDate = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                invoice.setPayDate(payDate);
                System.out.println("DEBUG: Set pay date for CASH payment: " + payDate);

                // Đảm bảo orderInfo được thiết lập
                Customer customer = invoice.getCustomer();
                if (invoice.getOrderInfo() == null || !invoice.getOrderInfo().contains("#")) {
                    String orderInfo = "Thanh toán đơn hàng #" + invoice.getId();
                    if (customer != null) {
                        orderInfo += " của KH " + customer.getFullName();
                    }
                    invoice.setOrderInfo(orderInfo);
                    System.out.println("DEBUG: Enforced orderInfo for CASH payment: " + orderInfo);
                }
            }

            // Đảm bảo hóa đơn luôn có thông tin nhân viên xử lý
            if (invoice.getEmployee() == null) {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated() &&
                            !"anonymousUser".equals(authentication.getPrincipal())) {
                        String username = authentication.getName();
                        System.out.println("DEBUG: Updating Employee for invoice - Current authenticated user: " + username);

                        // Tìm nhân viên theo username
                        Optional<Employee> employeeOpt = iEmployeeService.findByUsername(username);
                        if (employeeOpt.isPresent()) {
                            invoice.setEmployee(employeeOpt.get());
                            System.out.println("DEBUG: Assigned employee: " + employeeOpt.get().getFullName() + " to invoice ID: " + invoice.getId());
                        } else {
                            System.err.println("ERROR: Employee not found for username: " + username);
                        }
                    } else {
                        System.err.println("ERROR: No authentication information found");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Error assigning employee: " + e.getMessage());
                    e.printStackTrace();
                    // Continue processing
                }
            } else {
                System.out.println("DEBUG: Invoice already has employee assigned: " + invoice.getEmployee().getFullName());
            }

            // Cập nhật số lượng trong kho
            try {
                updateProductStock(invoice);
                System.out.println("DEBUG: Product stock updated successfully");
            } catch (Exception e) {
                System.err.println("ERROR: Error updating product stock: " + e.getMessage());
                e.printStackTrace();
                // Continue processing
            }

            // Cập nhật số lần mua hàng
            try {
                updateCustomerPurchaseCount(invoice);
                System.out.println("DEBUG: Customer purchase count updated successfully");
            } catch (Exception e) {
                System.err.println("ERROR: Error updating customer purchase count: " + e.getMessage());
                e.printStackTrace();
                // Continue processing
            }

            // Lưu lại thông tin hóa đơn
            try {
                invoice = invoiceServiceImpl.updateInvoice(invoice); // Use updateInvoice
                System.out.println("DEBUG: Invoice updated successfully: " + invoice.getId());
                session.setAttribute("SUCCESS_MESSAGE", "Thanh toán thành công!");
            } catch (Exception e) {
                System.err.println("ERROR: Error updating invoice: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("ERROR_MESSAGE", "Có lỗi khi lưu hóa đơn: " + e.getMessage());
                return "redirect:/dashboard/sales/form?error=save_error";
            }

            // Kiểm tra nếu cần in hóa đơn
            if (printInvoice) {
                System.out.println("DEBUG: Auto-downloading invoice PDF");
                return "redirect:/dashboard/sales/auto-download-pdf/" + invoiceId;
            } else {
                System.out.println("DEBUG: Redirecting to invoice detail page");
                return "redirect:/dashboard/sales/invoice-pdf/" + invoiceId;
            }
        } catch (Exception e) {
            System.err.println("ERROR: Error processing successful payment: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("ERROR_MESSAGE", "Lỗi khi xử lý thanh toán: " + e.getMessage());
            return "redirect:/dashboard/sales/form?error=process_error";
        }
    }


    /**
     * Cập nhật số lần mua hàng của khách hàng
     */
    private void updateCustomerPurchaseCount(Invoice invoice) {
        try {
            Customer customer = invoice.getCustomer();
            if (customer != null) {
                Customer freshCustomer = iCustomerService.findCustomerById(invoice.getCustomer().getCustomerID());
                if (freshCustomer != null) {
                    int newCount = freshCustomer.getPurchaseCount() + 1;

                    try {
                        iCustomerService.updatePurchaseCount(freshCustomer.getCustomerID(), newCount);
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
                paymentDate = new java.text.SimpleDateFormat("yyyyMMddHHmmss").parse(invoice.getPayDate());
            } catch (Exception e) {
                // Nếu có lỗi chuyển đổi, dùng thời gian hiện tại
                paymentDate = new Date();
            }
        } else {
            paymentDate = new Date();
        }
        model.addAttribute("payDate", paymentDate);

        // Thêm phương thức thanh toán vào model
        model.addAttribute("paymentMethod", invoice.getPaymentMethod());

        return "dashboard/sales/payment-invoice";
    }

    @GetMapping("/payment-callback")
    public String paymentCallback(HttpServletRequest request,
                                  HttpSession session,
                                  Model model) {
        try {
            // Log thông tin session để debug
            System.out.println("Session attributes in callback: ");
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                System.out.println("  " + name + ": " + session.getAttribute(name));
            }

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
                System.err.println("Invalid signature from VNPay");
                session.setAttribute("ERROR_MESSAGE", "Chữ ký không hợp lệ từ VNPay");
                return "redirect:/dashboard/sales/form?error=invalid_signature";
            }

            String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
            System.out.println("VNPay response code: " + vnpResponseCode);

            // Lấy ID hóa đơn từ tham số VNPay trước tiên
            Long invoiceId = null;
            String txnRef = vnpParams.get("vnp_TxnRef");
            if (txnRef != null) {
                try {
                    invoiceId = Long.parseLong(txnRef);
                    System.out.println("Invoice ID from txnRef: " + invoiceId);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid txnRef format: " + txnRef);
                    session.setAttribute("ERROR_MESSAGE", "Invalid txnRef format: " + txnRef);
                    return "redirect:/dashboard/sales/form?error=invalid_txnref_format";
                }
            }

            // Nếu không tìm thấy trong tham số, thử lấy từ session
            if (invoiceId == null) {
                invoiceId = (Long) session.getAttribute("invoiceId");
                System.out.println("Invoice ID from session: " + invoiceId);
                if (invoiceId == null) {
                    System.err.println("Missing invoice ID in both txnRef and session");
                    session.setAttribute("ERROR_MESSAGE", "Missing invoice ID");
                    return "redirect:/dashboard/sales/form?error=missing_invoice_id";
                }
            }

            Boolean printInvoice = (Boolean) session.getAttribute("printInvoice");
            System.out.println("Print invoice flag: " + printInvoice);

            // Tìm hóa đơn trước khi xử lý
            Invoice invoice = iInvoiceService.findById(invoiceId);
            if (invoice == null) {
                System.err.println("Invoice not found with ID: " + invoiceId);
                session.setAttribute("ERROR_MESSAGE", "Không tìm thấy hóa đơn!");
                return "redirect:/dashboard/sales/form?error=invoice_not_found";
            }

            System.out.println("Found invoice: " + invoice);

            // Kiểm tra kết quả thanh toán
            if ("00".equals(vnpResponseCode)) {
                System.out.println("Payment successful, updating invoice...");
                // Thanh toán thành công - Cập nhật thông tin hóa đơn với dữ liệu từ VNPay
                try {
                    System.out.println("Invoice before update: " + invoice);

                    // Cập nhật thông tin từ VNPay
                    invoice.setVnp_TxnRef(vnpParams.get("vnp_TxnRef"));

                    // Cập nhật phương thức thanh toán và trạng thái
                    invoice.setPaymentMethod(PaymentMethod.VNPAY);
                    invoice.setStatus(InvoiceStatus.SUCCESS);

                    // Lấy thông tin nhân viên đang đăng nhập để gán vào hóa đơn
                    try {
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null && authentication.isAuthenticated() &&
                                !"anonymousUser".equals(authentication.getPrincipal())) {
                            String username = authentication.getName();
                            System.out.println("Current authenticated user: " + username);
                            // Tìm nhân viên theo username
                            Optional<Employee> employeeOpt = iEmployeeService.findByUsername(username);
                            if (employeeOpt.isPresent()) {
                                invoice.setEmployee(employeeOpt.get());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error assigning employee: " + e.getMessage());
                        e.printStackTrace();
                        // Vẫn tiếp tục xử lý, không dừng tiến trình
                    }

                    // Cập nhật các thông tin khác từ VNPay
                    String orderInfo = vnpParams.get("vnp_OrderInfo");
                    if (orderInfo != null && !orderInfo.isEmpty()) {
                        invoice.setOrderInfo(orderInfo);
                    }

                    String payDate = vnpParams.get("vnp_PayDate");
                    if (payDate != null && !payDate.isEmpty()) {
                        invoice.setPayDate(payDate);
                    }

                    String transactionNo = vnpParams.get("vnp_TransactionNo");
                    if (transactionNo != null && !transactionNo.isEmpty()) {
                        invoice.setTransactionNo(transactionNo);
                    }

                    // Log thông tin hóa đơn sau khi cập nhật
                    System.out.println("Invoice after update: " + invoice);

                    try {
                        // Lưu hóa đơn đã cập nhật
                        invoice = iInvoiceService.saveInvoice(invoice);
                        System.out.println("Invoice saved successfully: " + invoice.getId());

                        // Xử lý thanh toán thành công (cập nhật số lượng sản phẩm với số lần mua hàng)
                        return processSuccessfulPayment(invoiceId, Boolean.TRUE.equals(printInvoice), session);
                    } catch (Exception e) {
                        System.err.println("Error saving updated invoice: " + e.getMessage());
                        e.printStackTrace();

                        // Nếu lỗi xảy ra khi lưu hóa đơn, thử xử lý thanh toán mà không cập nhật thông tin từ VNPAY
                        System.out.println("Trying to process payment without updating VNPAY information");
                        return processSuccessfulPayment(invoiceId, Boolean.TRUE.equals(printInvoice), session);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating invoice with VNPay data: " + e.getMessage());
                    e.printStackTrace();
                    session.setAttribute("ERROR_MESSAGE", e.getMessage());
                    return "redirect:/dashboard/sales/form?error=update_invoice_error";
                }
            } else {
                // Thanh toán thất bại - cập nhật trạng thái thành FAILED
                System.out.println("Payment failed with code: " + vnpResponseCode);
                invoice.setStatus(InvoiceStatus.FAILED);
                
                // Đảm bảo hóa đơn thất bại vẫn lưu thông tin nhân viên
                if (invoice.getEmployee() == null) {
                    try {
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null && authentication.isAuthenticated() &&
                                !"anonymousUser".equals(authentication.getPrincipal())) {
                            String username = authentication.getName();
                            System.out.println("Current authenticated user (failed payment): " + username);
                            
                            // Tìm nhân viên theo username
                            Optional<Employee> employeeOpt = iEmployeeService.findByUsername(username);
                            if (employeeOpt.isPresent()) {
                                invoice.setEmployee(employeeOpt.get());
                                System.out.println("Assigned employee to failed invoice: " + employeeOpt.get().getFullName());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error assigning employee to failed invoice: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                iInvoiceService.saveInvoice(invoice);

                return "redirect:/dashboard/sales/form?error=payment_failed&code=" + vnpResponseCode;
            }
        } catch (Exception e) {
            System.err.println("Error processing VNPay callback: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("ERROR_MESSAGE", "Lỗi xử lý callback từ VNPay: " + e.getMessage());
            return "redirect:/dashboard/sales/form?error=callback_error";
        }
    }

    /**
     * Tự động tải xuống file PDF hóa đơn
     */
    @GetMapping("/auto-download-pdf/{invoiceId}")
    public String autoDownloadInvoicePdf(@PathVariable Long invoiceId, Model model, HttpServletResponse response) {
        Invoice invoice = iInvoiceService.findById(invoiceId);
        if (invoice == null) {
            return "redirect:/dashboard/sales/form?error=invoice_not_found";
        }

        System.out.println("DEBUG - Auto-download PDF cho hóa đơn ID: " + invoiceId);

        model.addAttribute("invoice", invoice);
        model.addAttribute("transactionId", invoice.getId());
        model.addAttribute("amount", invoice.getAmount());
        model.addAttribute("orderInfo", invoice.getOrderInfo());
        model.addAttribute("autoDownload", true);
        model.addAttribute("downloadUrl", "/dashboard/sales/download-invoice-pdf/" + invoiceId);
        System.out.println("DEBUG - Đã thiết lập thuộc tính autoDownload: true");
        System.out.println("DEBUG - Đã thiết lập thuộc tính downloadUrl: " + "/dashboard/sales/download-invoice-pdf/" + invoiceId);

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

        // Thêm phương thức thanh toán vào model
        model.addAttribute("paymentMethod", invoice.getPaymentMethod());

        return "dashboard/sales/auto-download-invoice";
    }
}

