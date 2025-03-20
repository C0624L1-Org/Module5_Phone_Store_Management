package com.example.md5_phone_store_management.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.example.md5_phone_store_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.md5_phone_store_management.model.Cart;
import com.example.md5_phone_store_management.model.CartItem;
import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Employee;
import com.example.md5_phone_store_management.model.Invoice;
import com.example.md5_phone_store_management.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class SalesController {

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/dashboard/sales/form")
    public String showSalesForm(Model model,
                                Pageable pageable,
                                HttpSession session,
                                @RequestParam(required = false) Integer customerId) {

        List<Customer> customers = customerService.findAllCustomers();
        model.addAttribute("customers", customers);


        Page<Product> productPage = iProductService.findAll(pageable);
        model.addAttribute("products", productPage.getContent());

        if (customerId != null) {
            Customer customer = customerService.getCustomerByID(customerId);
            if (customer != null) {
                Cart cart = cartService.getActiveCartForCustomer(customer);
                if (cart != null) {
                    model.addAttribute("cart", cart);
                    model.addAttribute("cartItems", cartService.getCartItems(cart));
                    model.addAttribute("cartTotal", cartService.getCartTotal(cart));
                }
                session.setAttribute("currentCustomer", customer);
            }
        }

        return "dashboard/sales/form";
    }

    @GetMapping("/dashboard/sales/add-to-cart")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam Integer quantity,
                            @RequestParam(required = false) Integer customerId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Customer customer;
        if (customerId != null) {
            customer = customerService.getCustomerByID(customerId);
        } else {
            customer = (Customer) session.getAttribute("currentCustomer");
        }

        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn khách hàng trước khi thêm sản phẩm vào giỏ");
            return "redirect:/dashboard/sales/form";
        }

        // Get the product
        Product product = iProductService.getProductById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }

        if (product.getStockQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error",
                    "Số lượng trong kho không đủ. Hiện chỉ còn " + product.getStockQuantity() + " sản phẩm.");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }

        Cart cart = cartService.getActiveCartForCustomer(customer);

        cartService.addItemToCart(cart, product, quantity);

        redirectAttributes.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng");
        return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
    }

    @GetMapping("/dashboard/sales/remove-from-cart/{productId}")
    public String removeFromCart(@PathVariable Integer productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Customer customer = (Customer) session.getAttribute("currentCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
            return "redirect:/dashboard/sales/form";
        }

        Product product = iProductService.getProductById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }

        Cart cart = cartService.getActiveCartForCustomer(customer);
        cartService.removeItemFromCart(cart, product);

        redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng");
        return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
    }

    @GetMapping("/dashboard/sales/update-quantity")
    public String updateQuantity(@RequestParam Integer productId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Customer customer = (Customer) session.getAttribute("currentCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
            return "redirect:/dashboard/sales/form";
        }

        Product product = iProductService.getProductById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }

        if (product.getStockQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error",
                    "Số lượng trong kho không đủ. Hiện chỉ còn " + product.getStockQuantity() + " sản phẩm.");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }

        Cart cart = cartService.getActiveCartForCustomer(customer);
        cartService.updateItemQuantity(cart, product, quantity);

        redirectAttributes.addFlashAttribute("success", "Đã cập nhật số lượng sản phẩm");
        return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
    }

    @GetMapping("/dashboard/sales/clear-cart")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("currentCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
            return "redirect:/dashboard/sales/form";
        }

        Cart cart = cartService.getActiveCartForCustomer(customer);
        cartService.clearCart(cart);

        redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng");
        return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
    }

    @PostMapping("/dashboard/sales/checkout")
    public String checkout(@RequestParam(value = "customerId", required = false) Integer customerId,
                           @RequestParam(value = "paymentMethod", required = true) String paymentMethod,
                           HttpServletRequest request,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        // Get customer
        Customer customer;
        if (customerId != null) {
            customer = customerService.getCustomerByID(customerId);
        } else {
            customer = (Customer) session.getAttribute("currentCustomer");
        }

        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn khách hàng");
            return "redirect:/dashboard/sales/form";
        }

        // Get cart
        Cart cart = cartService.getActiveCartForCustomer(customer);
        List<CartItem> cartItems = cartService.getCartItems(cart);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }

        // Get current employee
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Employee> employeeOptional = iEmployeeService.findByUsername(username);
        if (!employeeOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin nhân viên");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }
        Employee employee = employeeOptional.get();

        // Prepare data for processing
        BigDecimal totalAmount = cartService.getCartTotal(cart);
        List<Product> products = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();

            if (product.getStockQuantity() < quantity) {
                redirectAttributes.addFlashAttribute("error",
                        "Số lượng sản phẩm " + product.getName() + " trong kho không đủ. Hiện chỉ còn " +
                                product.getStockQuantity() + " sản phẩm.");
                return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
            }

            products.add(product);
            quantities.add(quantity);
        }

        session.setAttribute("customer", customer);
        session.setAttribute("employee", employee);
        session.setAttribute("products", products);
        session.setAttribute("quantities", quantities);
        session.setAttribute("totalAmount", totalAmount);
        session.setAttribute("paymentMethod", paymentMethod);
        session.setAttribute("cartId", cart.getId());

        // Process payment based on method
        if ("VNPAY".equals(paymentMethod)) {

            Long orderId = generateOrderId();
            session.setAttribute("orderId", orderId);

            // Redirect to the VNPayController for payment processing
            return "redirect:/api/vnpay/create-direct-payment";
        } else if ("CASH".equals(paymentMethod)) {
            // Process cash payment
            processPayment(session, "CASH", null);

            // Mark cart as inactive
            cartService.markCartAsInactive(cart);

            redirectAttributes.addFlashAttribute("success", "Thanh toán tiền mặt thành công!");
            return "redirect:/dashboard/sales/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ");
            return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
        }
    }

    private Long generateOrderId() {
        return System.currentTimeMillis() + new Random().nextInt(1000);
    }

    // Process payment and create invoice
    private Invoice processPayment(HttpSession session, String paymentMethod, String transactionId) {
        Customer customer = (Customer) session.getAttribute("customer");
        Employee employee = (Employee) session.getAttribute("employee");
        List<Product> products = (List<Product>) session.getAttribute("products");
        List<Integer> quantities = (List<Integer>) session.getAttribute("quantities");
        BigDecimal totalAmount = (BigDecimal) session.getAttribute("totalAmount");

        // Create and save invoice
        Invoice invoice = new Invoice();
        invoice.setVnp_TxnRef(transactionId != null ? transactionId : "CASH-" + System.currentTimeMillis());
        invoice.setAmount(totalAmount.longValue());
        invoice.setOrderInfo("Thanh toán đơn hàng");
        invoice.setBankCode(paymentMethod.equals("CASH") ? "CASH" : "VNPAY");
        invoice.setCustomer(customer);
        invoice.setEmployee(employee);
        invoice.setProducts(products);
        invoice.setProductQuantities(quantities);

        Invoice savedInvoice = invoiceService.saveInvoice(invoice);

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantity = quantities.get(i);

            product.setStockQuantity(product.getStockQuantity() - quantity);
            iProductService.updateProductWithSellingPrice(product);
        }

        // Clear session data
        session.removeAttribute("customer");
        session.removeAttribute("employee");
        session.removeAttribute("products");
        session.removeAttribute("quantities");
        session.removeAttribute("totalAmount");
        session.removeAttribute("paymentMethod");

        return savedInvoice;
    }

    // Handle VNPAY payment callback
    @GetMapping("/api/vnpay/payment-callback")
    public String paymentCallback(@RequestParam Map<String, String> params,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String vnpResponseCode = params.get("vnp_ResponseCode");

        if ("00".equals(vnpResponseCode)) {
            // Payment successful
            String transactionId = params.get("vnp_TransactionNo");
            Invoice invoice = processPayment(session, "VNPAY", transactionId);

            Integer cartId = (Integer) session.getAttribute("cartId");
            if (cartId != null) {
                Cart cart = cartService.getCartById(cartId);
                if (cart != null) {
                    cartService.markCartAsInactive(cart);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Thanh toán VNPAY thành công! Mã giao dịch: " + transactionId);
            return "redirect:/dashboard/sales/form";
        } else {
            // Payment failed
            Customer customer = (Customer) session.getAttribute("customer");
            redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại. Mã lỗi: " + vnpResponseCode);

            if (customer != null) {
                return "redirect:/dashboard/sales/form?customerId=" + customer.getCustomerID();
            } else {
                return "redirect:/dashboard/sales/form";
            }
        }
    }
}
