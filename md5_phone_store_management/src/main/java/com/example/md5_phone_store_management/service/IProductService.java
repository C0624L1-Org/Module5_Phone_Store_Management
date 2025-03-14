package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductService {
    //Tuấn Anh
    Page<Product> findAll(Pageable pageable);
    Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, double purchasePrice, Pageable pageable);

    //Đình Anh
    void saveProduct(Product product);
    void saveProductWithImg(Product product, List<MultipartFile> files);
}