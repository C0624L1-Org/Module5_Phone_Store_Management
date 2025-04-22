package com.example.md5_phone_store_management.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import com.example.md5_phone_store_management.model.dto.SearchNavbarDTO;
import com.example.md5_phone_store_management.service.implement.ProductService;

@RestController
@RequestMapping("/api/search")
public class SearchNavbarRestController {

    @Autowired
    private ProductService productService;

    private SearchNavbarDTO convertToDTO(Product product) {
        SearchNavbarDTO dto = new SearchNavbarDTO();
        dto.setProductId(product.getProductID());
        dto.setName(product.getName());
        dto.setRetailPrice(product.getRetailPrice());
        dto.setRetailPrice(product.getRetailPrice());
        dto.setCpu(product.getCPU());
        dto.setStorage(product.getStorage());

        // Lấy URL ảnh đầu tiên nếu có
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ProductImage firstImage = product.getImages().get(0);
            dto.setImageUrl(firstImage.getImageUrl());
        } else {
            dto.setImageUrl("/img/no-image-product.png");
        }

        return dto;
    }

    @GetMapping("/products")
    public ResponseEntity<List<SearchNavbarDTO>> searchProducts(@RequestParam String keyword) {
        System.out.println("API tìm kiếm được gọi với từ khóa: " + keyword);
        
        try {
            // Cho phép từ khóa rỗng, trả về danh sách rỗng
            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("Từ khóa rỗng, trả về danh sách rỗng");
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Giới hạn kết quả tìm kiếm
            Pageable pageable = PageRequest.of(0, 8);
            List<Product> products = productService.searchProductsNameForNavbar(keyword, pageable);

            System.out.println("Tìm thấy " + products.size() + " sản phẩm với từ khóa: " + keyword);

            List<SearchNavbarDTO> results = products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}
