package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.Supplier;
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
@RequestMapping("/dashboard/products")
public class ProductController {
    @Autowired
    ProductService productService;
    @Autowired
    SupplierService supplierService;
    @Autowired
    CloudinaryService cloudinaryService;

    // Tuấn Anh
    /*@GetMapping("/list")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "0", required = false) int page) {
        Pageable pageable = PageRequest.of(page, 2);
        Page<Product> listProducts = productService.findAll(pageable);
        model.addAttribute("listProducts", listProducts);
        return "dashboard/product/home-product";
    }*/

//    @GetMapping("/list")
//    public String search1(Model model,
//                          @RequestParam(name = "searchProduct", required = false) String searchProduct,
//                          @RequestParam(name = "searchSupplier", required = false) String searchSupplier,
//                          @RequestParam(name = "rangePrice", required = false) Integer rangePrice,
//                          @RequestParam(name = "page", defaultValue = "0", required = false) int page,
//                          HttpSession session) {
//        Pageable pageable = PageRequest.of(page, 5);
//        Page<Product> listProducts;
//
//        if (searchProduct != null || searchSupplier != null || (rangePrice != null && rangePrice > 0)) {
//            listProducts = productService.searchProductByNameAndSupplier_NameAndPurchasePrice(
//                    searchProduct, searchSupplier, rangePrice, pageable);
//        } else {
//            listProducts = productService.findAll(pageable);
//        }
//
//        List<Long> selectedProductIds = (List<Long>) session.getAttribute("selectedProductIds");
//        if (selectedProductIds == null) {
//            selectedProductIds = new ArrayList<>();
//        }
//
//        model.addAttribute("listProducts", listProducts);
//        model.addAttribute("selectedProductIds", selectedProductIds); // Truyền danh sách ID đã chọn
//        model.addAttribute("searchProduct", searchProduct);
//        model.addAttribute("searchSupplier", searchSupplier);
//        model.addAttribute("rangePrice", rangePrice);
//
//        String successMessage = (String) session.getAttribute("SUCCESS_MESSAGE");
//        if (successMessage != null) {
//            model.addAttribute("message", successMessage);
//            session.removeAttribute("SUCCESS_MESSAGE");
//        }
//
//        return "dashboard/product/home-product";
//    }
@GetMapping("/list")
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

    return "dashboard/product/home-product";
}




    @PostMapping("/update-prices")
    public String updatePrices(@RequestParam("productIds") List<Long> productIds,
                               @RequestParam(value = "newPrices", required = false) List<String> newPrices,
                               @RequestParam(value = "oldPrices", required = false) List<String> oldPrices,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra kích thước danh sách cơ bản
            if (productIds == null || productIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không có sản phẩm nào được chọn!");
                return "redirect:/dashboard/products/list";
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
                return "redirect:/dashboard/products/list";
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
                        if (newPrice <= 0) newPrice = oldPrice; // Giá mới không hợp lệ thì dùng giá cũ
                    } catch (NumberFormatException e) {
                        newPrice = oldPrice; // Nếu không parse được, dùng giá cũ
                    }
                } else {
                    newPrice = oldPrice; // Nếu không điền giá mới, dùng giá cũ
                }

                Product product = productService.findById(Math.toIntExact(productId));
                if (product != null) {
                    // Chỉ cập nhật nếu giá mới khác giá hiện tại hoặc giá hiện tại là null mà giá mới được điền
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
            return "redirect:/dashboard/products/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật giá bán lẻ: " + e.getMessage());
            return "redirect:/dashboard/products/list";
        }
    }

    // Đình Anh
    @GetMapping("/create-form")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("supplier", supplierService.getSupplierList());
        return "dashboard/product/create-product-form";
    }

    @PostMapping("/add-product")
    public String addProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                             BindingResult biResult,
                             @RequestParam(value = "imgProducts", required = false) List<MultipartFile> imgProducts,
                             RedirectAttributes redirectAttr,
                             Model model) {
        if (biResult.hasErrors()) {
            model.addAttribute("supplier", supplierService.getSupplierList());
            return "dashboard/product/create-product-form";
        }

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        Supplier supplier = supplierService.getSupplier(productDTO.getSupplierID());
        if (supplier == null) {
            model.addAttribute("error", "Nhà cung cấp không hợp lệ!");
            model.addAttribute("supplier", supplierService.getSupplierList());
            return "dashboard/product/create-product-form";
        }
        product.setSupplier(supplier);
        //product.setRetailPrice(new BigDecimal(0));

        // Nếu có file ảnh được upload
        if (imgProducts != null && !imgProducts.isEmpty()
                && imgProducts.stream().anyMatch(file -> file.getSize() > 0)) {
            for (MultipartFile file : imgProducts) {
                if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                    redirectAttr.addFlashAttribute("messageType", "error");
                    redirectAttr.addFlashAttribute("message", "Kích thước ảnh quá lớn!");
                    return "redirect:/dashboard/products/create-form";
                }
                if (!file.getContentType().startsWith("image/")) {
                    redirectAttr.addFlashAttribute("messageType", "error");
                    redirectAttr.addFlashAttribute("message", "Định dạng ảnh không hợp lệ!");
                    return "redirect:/dashboard/products/create-form";
                }
            }
            // Lưu kèm danh sách ảnh
            productService.saveProductWithImg(product, imgProducts);
        } else {
            // Không có file upload nào, chỉ lưu sản phẩm
            productService.saveProduct(product);
        }

        redirectAttr.addFlashAttribute("messageType", "success");
        redirectAttr.addFlashAttribute("message", "Đã Thêm mới thành công!");
        return "redirect:/dashboard/products/list";
    }

    //update
    @GetMapping("/update-form/{id}")
    public String updateForm(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy sản phẩm");
            return "redirect:/dashboard/products/list";
        }
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        productDTO.setSupplierID(product.getSupplier().getSupplierID());
        System.out.println("Before updating: " + productDTO.toString());
        model.addAttribute("productDTO", productDTO);
        model.addAttribute("supplier", supplierService.getSupplierList());
        return "dashboard/product/update-product-form";
    }

    @PostMapping("/update-product")
    public String updateProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                BindingResult biResult,
                                @RequestParam(value = "imgProducts", required = false) List<MultipartFile> imgProducts,
                                RedirectAttributes redirectAttr,
                                Model model) {
        System.out.println("After updating: " + productDTO.toString());
        if (biResult.hasErrors()) {
            System.out.println(biResult.getAllErrors().get(0).getDefaultMessage());
            return "dashboard/product/update-product-form";
        }

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product.setSupplier(supplierService.getSupplier(productDTO.getSupplierID()));

        // Nếu có file ảnh được upload
        if (imgProducts != null && !imgProducts.isEmpty()
                && imgProducts.stream().anyMatch(file -> file.getSize() > 0)) {
            //delete old product images
            productService.deleteProductImages(product);
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : imgProducts) {
                if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                    redirectAttr.addFlashAttribute("messageType", "error");
                    redirectAttr.addFlashAttribute("message", "Kích thước ảnh quá lớn!");
                    return "redirect:/dashboard/products/update-form/" + product.getProductID();
                }
                if (!file.getContentType().startsWith("image/")) {
                    redirectAttr.addFlashAttribute("messageType", "error");
                    redirectAttr.addFlashAttribute("message", "Định dạng ảnh không hợp lệ!");
                    return "redirect:/dashboard/products/create-form/" + product.getProductID();
                }
                try {
                    String imageUrl = cloudinaryService.uploadFile(file, "product");
                    ProductImage pi = new ProductImage();
                    pi.setImageUrl(imageUrl);
                    pi.setProduct(product);
                    productImages.add(pi);
                    productService.saveProductImage(product, pi); //save new product image into db
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi upload ảnh sản phẩm", e);
                }
            }
        }
        System.out.println("Product: " + product.toString());
        productService.updateProductWithSellingPrice(product);


        redirectAttr.addFlashAttribute("messageType", "success");
        redirectAttr.addFlashAttribute("message", "Đã Thêm mới thành công!");
        return "redirect:/dashboard/products/list";
    }

}