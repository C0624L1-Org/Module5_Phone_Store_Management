package com.example.md5_phone_store_management.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.ProductDTO;
import com.example.md5_phone_store_management.service.CloudinaryService;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.SupplierService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/dashboard/products")
public class ProductController {
    @Autowired
    ProductService productService;
    @Autowired
    SupplierService supplierService;
    @Autowired
    CloudinaryService cloudinaryService;

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
        redirectAttr.addFlashAttribute("message", "Đã cập nhật thành công!");
        return "redirect:/dashboard/products/list";
    }

    @GetMapping("/detail/{id}")
    public String viewProductDetail(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            redirectAttributes.addFlashAttribute("messageType", "error");
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy sản phẩm");
            return "redirect:/dashboard/products/list";
        }
        
        model.addAttribute("product", product);
        return "dashboard/product/product-detail";
    }

}