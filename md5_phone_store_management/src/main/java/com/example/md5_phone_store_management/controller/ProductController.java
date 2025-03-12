package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.Supplier;
import com.example.md5_phone_store_management.model.dto.ProductDTO;
import com.example.md5_phone_store_management.service.implement.ProductService;
import com.example.md5_phone_store_management.service.implement.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    // làm tạm để kiểm thử
    @GetMapping
    public String home(){
        return "dashboard/product/home-product";
    }

    @GetMapping("/create-form")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("supplier", supplierService.getSupplierList());
        return "dashboard/product/create-product-form";
    }

    @PostMapping("/add-product")
    public String addProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                             BindingResult biResult,
                             RedirectAttributes redirectAttr,
                             Model model) {
        if (biResult.hasErrors()) {
            model.addAttribute("supplier",supplierService.getSupplierList());
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
        productService.saveProduct(product);
        redirectAttr.addFlashAttribute("message","Đã Thêm mới thành công");
        return "redirect:/dashboard/products";
    }
}
