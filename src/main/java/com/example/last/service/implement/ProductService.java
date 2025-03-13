package com.example.last.service.implement;

import com.example.last.model.Product;
import com.example.last.repository.IProductRepository;
import com.example.last.service.IProductService;
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
