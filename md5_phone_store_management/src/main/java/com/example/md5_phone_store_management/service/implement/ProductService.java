package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.CloudinaryService;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService implements IProductService {
    @Autowired
    IProductRepository productRepository;

    @Autowired
    CloudinaryService cloudinaryService;
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
                product.getStockQuantity(),
                product.getQrCode(),
                product.getSupplier().getSupplierID());
    }

    @Override
    public void saveProductWithImg(Product product, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadFile(file, "product");
                        ProductImage pi = new ProductImage();
                        pi.setImageUrl(imageUrl);
                        pi.setProduct(product);
                        productImages.add(pi);
                    } catch (IOException e) {
                        throw new RuntimeException("Lỗi khi upload ảnh sản phẩm", e);
                    }
                }
            }
            // Gán danh sách ảnh đã tạo vào product
            product.setImages(productImages);
        }
        // Lưu sản phẩm không có ảnh)
        productRepository.save(product);
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