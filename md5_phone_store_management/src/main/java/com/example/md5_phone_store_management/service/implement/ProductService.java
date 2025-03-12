package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {

    @Autowired
    private IProductRepository iProductRepository;

    @Override
    public void saveProduct(Product product) {
        iProductRepository.saveProduct(product.getName(),
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
}
