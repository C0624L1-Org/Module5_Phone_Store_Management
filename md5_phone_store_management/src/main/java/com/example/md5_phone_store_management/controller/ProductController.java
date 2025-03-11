package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.service.implement.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("dashboard/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @GetMapping("")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "0", required = false) int page){
        Pageable pageable = PageRequest.of(page,2);
        Page<Product> listProducts = productService.findAll(pageable);
        model.addAttribute("listProducts",listProducts);
        return "dashboard/admin/product/home-product";
    }

    @GetMapping("/search")
    public String search(Model model,@RequestParam(name="searchProduct",required=false) String searchProduct,
                         @RequestParam(name="searchSupplier",required=false) String searchSupplier,
                         @RequestParam(name="rangePrice",required=false) double rangePrice,
                         @RequestParam(name="page",defaultValue = "0")int page){
        Pageable pageable = PageRequest.of(page,2);
        Page<Product> listProducts= productService.searchProductByNameAndSupplier_NameAndPurchasePrice(searchProduct,searchSupplier,rangePrice,pageable);
        model.addAttribute("listProducts",listProducts);
        return "dashboard/admin/product/home-product";
    }

}
