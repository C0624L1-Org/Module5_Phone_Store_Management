package com.example.md5_phone_store_management.controller;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.service.implement.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard/products")
public class RestProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/select-product")
    public ResponseEntity<Map<String, Integer>> selectProduct(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long productId = Long.parseLong(payload.get("productId").toString());
        boolean isChecked = Boolean.parseBoolean(payload.get("isChecked").toString());
        System.out.println("Received productId: " + productId + ", isChecked: " + isChecked); // Debug

        List<Long> selectedProductIds = (List<Long>) session.getAttribute("selectedProductIds");
        if (selectedProductIds == null) {
            selectedProductIds = new ArrayList<>();
        }

        if (isChecked) {
            if (!selectedProductIds.contains(productId)) {
                selectedProductIds.add(productId);
            }
        } else {
            selectedProductIds.remove(productId); // Xóa productId khi uncheck
        }

        session.setAttribute("selectedProductIds", selectedProductIds);

        Map<String, Integer> response = new HashMap<>();
        response.put("selectedCount", selectedProductIds.size());
        System.out.println("Selected products: " + selectedProductIds); // Debug
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deselect-products")
    public ResponseEntity<Map<String, Integer>> deselectProducts(HttpSession session) {
        session.setAttribute("selectedProductIds", new ArrayList<>());
        Map<String, Integer> response = new HashMap<>();
        response.put("selectedCount", 0);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/selected-products")
    public ResponseEntity<?> getSelectedProducts(HttpSession session) {
        List<Long> selectedProductIds = (List<Long>) session.getAttribute("selectedProductIds");
        if (selectedProductIds == null || selectedProductIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn ít nhất một sản phẩm!");
        }

        if (selectedProductIds.size() > 5) {
            return ResponseEntity.badRequest().body("Vui lòng chọn ít hơn hoặc bằng 5 sản phẩm!");
        }

        List<Product> selectedProducts = productService.findAllByIds(selectedProductIds);
        return ResponseEntity.ok(selectedProducts);
    }
}