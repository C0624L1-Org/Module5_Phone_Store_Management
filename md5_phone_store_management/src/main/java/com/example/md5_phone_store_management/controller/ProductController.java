package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Role;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.ProductDTO;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.SupplierService;
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

import java.util.List;

@Controller
@RequestMapping("/dashboard/products")
public class ProductController {
    @Autowired
    ProductService productService;
    @Autowired
    SupplierService supplierService;
    // Tuấn Anh
    @GetMapping("/list")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "0", required = false) int page){
        Pageable pageable = PageRequest.of(page,2);
        Page<Product> listProducts = productService.findAll(pageable);
        model.addAttribute("listProducts",listProducts);
        return "dashboard/product/home-product";
    }

    @GetMapping("/search")
    public String search(Model model,@RequestParam(name="searchProduct",required=false) String searchProduct,
                         @RequestParam(name="searchSupplier",required=false) String searchSupplier,
                         @RequestParam(name="rangePrice",required=false) double rangePrice,
                         @RequestParam(name="page",defaultValue = "0")int page){
        Pageable pageable = PageRequest.of(page,2);
        Page<Product> listProducts= productService.searchProductByNameAndSupplier_NameAndPurchasePrice(searchProduct,searchSupplier,rangePrice,pageable);
        model.addAttribute("listProducts",listProducts);
        return "dashboard/product/home-product";
    }
    // Đình Anh
    @GetMapping("/create-form")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("supplier",supplierService.getSupplierList());
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


}