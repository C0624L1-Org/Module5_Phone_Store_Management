package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface IProductService {
    //Tuấn Anh
    Page<Product> findAll(Pageable pageable);
    Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, double purchasePrice, Pageable pageable);

    //Đình Anh
    void saveProduct(Product product);
    void saveProductWithImg(Product product, List<MultipartFile> files);

    //update
    Product getProductById(Integer id);
    void updateProductWithSellingPrice(Product product);
    void saveProductImage(Product product, ProductImage productImage);
    void deleteProductImages(Product product);
}