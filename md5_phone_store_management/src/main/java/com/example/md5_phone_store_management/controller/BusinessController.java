package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import com.example.md5_phone_store_management.model.dto.ProductDTO;
import com.example.md5_phone_store_management.service.CloudinaryService;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.SupplierService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("")
public class BusinessController {

    @Autowired
    ProductService productService;
    @Autowired
    SupplierService supplierService;
    @Autowired
    CloudinaryService cloudinaryService;


    //    trang chính quản lý
//    http://localhost:8080/dashboard/business/management
    @GetMapping("/dashboard/business/management")
    public String showManagementPage(Model model, HttpSession session) {
        return "/business/business-home";
    }




    @GetMapping("/dashboard/products/listToChoose")
    public String search1(Model model,
                          @RequestParam(name = "searchProduct", required = false) String searchProduct,
                          @RequestParam(name = "searchSupplier", required = false) String searchSupplier,
                          @RequestParam(name = "rangePrice", required = false) Integer rangePrice,
                          @RequestParam(name = "haveRetailPrice", required = false, defaultValue = "yes") String haveRetailPrice, // Thêm tham số mới
                          @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                          HttpSession session) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Product> listProducts;

        // Điều kiện lọc dựa trên haveRetailPrice
        if (searchProduct != null || searchSupplier != null || (rangePrice != null && rangePrice > 0) || "no".equals(haveRetailPrice)) {
            listProducts = productService.searchProductByNameAndSupplier_NameAndPurchasePriceAndRetailPrice(
                    searchProduct, searchSupplier, rangePrice, "no".equals(haveRetailPrice), pageable);
        } else {
            listProducts = productService.findAll(pageable); // Mặc định là "Tất cả sản phẩm"
        }

        List<Long> selectedProductIds = (List<Long>) session.getAttribute("selectedProductIds");
        if (selectedProductIds == null) {
            selectedProductIds = new ArrayList<>();
        }

        // Thêm các thuộc tính vào model
        model.addAttribute("listProducts", listProducts);
        model.addAttribute("selectedProductIds", selectedProductIds);
        model.addAttribute("searchProduct", searchProduct);
        model.addAttribute("searchSupplier", searchSupplier);
        model.addAttribute("rangePrice", rangePrice);
        model.addAttribute("haveRetailPrice", haveRetailPrice); // Trả lại giá trị haveRetailPrice

        String successMessage = (String) session.getAttribute("SUCCESS_MESSAGE");
        if (successMessage != null) {
            model.addAttribute("message", successMessage);
            session.removeAttribute("SUCCESS_MESSAGE");
        }

        return "/business/choose-product";

    }

    @PostMapping("/dashboard/products/update-prices")
    public String updatePrices(@RequestParam("productIds") List<Long> productIds,
                               @RequestParam(value = "newPrices", required = false) List<String> newPrices,
                               @RequestParam(value = "oldPrices", required = false) List<String> oldPrices,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra kích thước danh sách cơ bản
            if (productIds == null || productIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không có sản phẩm nào được chọn!");
                return "redirect:/dashboard/products/listToChoose";
            }

            // Đảm bảo newPrices và oldPrices không null, nếu null thì tạo danh sách rỗng
            if (newPrices == null) newPrices = new ArrayList<>();
            if (oldPrices == null) oldPrices = new ArrayList<>();

            // Điều chỉnh kích thước danh sách nếu cần
            while (newPrices.size() < productIds.size()) newPrices.add("");
            while (oldPrices.size() < productIds.size()) oldPrices.add("");

            // Kiểm tra kích thước sau khi điều chỉnh
            if (productIds.size() != newPrices.size() || productIds.size() != oldPrices.size()) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ!");
                return "redirect:/dashboard/products/listToChoose";
            }

            StringBuilder successMessage = new StringBuilder("Cập nhật giá cho ");
            List<String> updatedNames = new ArrayList<>();

            for (int i = 0; i < productIds.size(); i++) {
                Long productId = productIds.get(i);
                String newPriceStr = newPrices.get(i);
                String oldPriceStr = oldPrices.get(i);

                // Xử lý giá cũ
                Integer oldPrice = null;
                if (oldPriceStr != null && !oldPriceStr.trim().isEmpty() && !"null".equals(oldPriceStr)) {
                    try {
                        oldPrice = Integer.parseInt(oldPriceStr);
                    } catch (NumberFormatException e) {
                        oldPrice = null; // Nếu không parse được, coi như không có giá cũ
                    }
                }

                // Xử lý giá mới
                Integer newPrice = null;
                if (newPriceStr != null && !newPriceStr.trim().isEmpty()) {
                    try {
                        newPrice = Integer.parseInt(newPriceStr);
                        if (newPrice <= 0) newPrice = oldPrice;
                    } catch (NumberFormatException e) {
                        newPrice = oldPrice;
                    }
                } else {
                    newPrice = oldPrice;
                }

                Product product = productService.findById(Math.toIntExact(productId));
                if (product != null) {

                    BigDecimal currentPrice = product.getRetailPrice();
                    if ((currentPrice == null && newPrice != null) ||
                            (currentPrice != null && newPrice != null && !currentPrice.equals(BigDecimal.valueOf(newPrice)))) {
                        product.setRetailPrice(newPrice != null ? BigDecimal.valueOf(newPrice) : null);
                        productService.save(product);
                        updatedNames.add(product.getName());
                    }
                }
            }

            if (updatedNames.isEmpty()) {
                session.setAttribute("SUCCESS_MESSAGE", "Không có thay đổi nào được áp dụng!");
            } else {
                successMessage.append(String.join(", ", updatedNames)).append(" thành công!");
                session.setAttribute("SUCCESS_MESSAGE", successMessage.toString());
            }

            session.setAttribute("selectedProductIds", new ArrayList<>());
            return "redirect:/dashboard/products/listToChoose";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật giá bán lẻ: " + e.getMessage());
            return "redirect:/dashboard/products/listToChoose";
        }
    }

    @GetMapping("/dashboard/products/update-form-retail-price/{id}")
    public String updateForm(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy sản phẩm");
            return "redirect:/dashboard/products/listToChoose";
        }
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        productDTO.setSupplierID(product.getSupplier().getSupplierID());
        System.out.println("Before updating: " + productDTO.toString());
        model.addAttribute("productDTO", productDTO);
        model.addAttribute("supplier", supplierService.getSupplierList());
        return "business/update-retail-price";
    }

    @PostMapping("/dashboard/products/update-product-retail-price")
    public String updateProductRetailPrice(
            @ModelAttribute("productDTO") ProductDTO productDTO,
            RedirectAttributes redirectAttr,
            Model model) {
        System.out.println("After binding: " + productDTO.toStringHau());
        try {
            Long productId = Long.valueOf(productDTO.getProductID());
            BigDecimal retailPrice = productDTO.getRetailPrice();

            Product product = productService.findById(productId.intValue());
            if (product == null) {
//                 lỗi tìm sp
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
            }
            if (retailPrice != null) {
                product.setRetailPrice(retailPrice);
                System.out.println(" giá mới là" + retailPrice);
                System.out.println("đang cập nhật");
                productService.save(product);
                redirectAttr.addFlashAttribute("messageType", "success");
                redirectAttr.addFlashAttribute("message", "Đã cập nhật giá bán lẻ thành công!");
            } else {
                redirectAttr.addFlashAttribute("messageType", "warning");
                redirectAttr.addFlashAttribute("message", "Giá bán lẻ mới không được cung cấp, không có thay đổi nào được thực hiện.");
            }

        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
            redirectAttr.addFlashAttribute("messageType", "error");
            redirectAttr.addFlashAttribute("message", "Cập nhật giá bán lẻ thất bại: " + e.getMessage());
        }

        return "redirect:/dashboard/products/listToChoose";
    }

}
