package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {
    @Autowired
    IProductRepository productRepository;
    // Đình Anh
    @Override
    public void saveProduct(Product product) {
        productRepository.saveProduct(product.getName(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getCPU(),
                product.getStorage(),
                product.getScreenSize(),
                product.getCamera(),
                product.getSelfie(),
                product.getDetailedDescription(),
                product.getImage(),
                product.getStockQuantity(),
                product.getQrCode(),
                product.getSupplier().getSupplierID());
    }
    //Tuấn Anh
    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    @Override
    public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, double purchasePrice, Pageable pageable) {
        return productRepository.searchProductByNameAndSupplier_NameAndPurchasePrice(name,supplierName,purchasePrice,pageable);
    }
}