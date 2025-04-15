//    package com.example.md5_phone_store_management.service.implement;
//
//    import java.io.IOException;
//    import java.util.ArrayList;
//    import java.util.List;
//    import java.util.stream.Collectors;
//
//    import org.springframework.beans.factory.annotation.Autowired;
//    import org.springframework.context.ApplicationEventPublisher;
//    import org.springframework.data.domain.Page;
//    import org.springframework.data.domain.Pageable;
//    import org.springframework.stereotype.Service;
//    import org.springframework.transaction.annotation.Transactional;
//    import org.springframework.web.multipart.MultipartFile;
//
//    import com.example.md5_phone_store_management.model.Product;
//    import com.example.md5_phone_store_management.model.ProductImage;
//    import com.example.md5_phone_store_management.repository.IProductRepository;
//    import com.example.md5_phone_store_management.service.CloudinaryService;
//    import com.example.md5_phone_store_management.service.IProductService;
//
//    @Service
//    @Transactional
//    public class ProductService implements IProductService {
//
//        @Autowired
//        private ApplicationEventPublisher eventPublisher;
//
//        @Autowired
//        IProductRepository productRepository;
//
//        @Autowired
//        CloudinaryService cloudinaryService;
//
//
//
//        @Override
//        public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePriceAndRetailPrice(
//                String name, String supplierName, Integer purchasePrice, boolean noRetailPrice, Pageable pageable) {
//            if (noRetailPrice) {
//                // Lọc các sản phẩm có retailPrice là null
//                return productRepository.findByNameContainingAndSupplierNameContainingAndPurchasePriceLessThanEqualAndRetailPriceIsNull(
//                        name != null ? name : "",
//                        supplierName != null ? supplierName : "",
//                        purchasePrice != null ? purchasePrice : Integer.MAX_VALUE,
//                        pageable);
//            } else {
//                // Lọc tất cả sản phẩm (bao gồm cả có và không có retailPrice)
//                return productRepository.findByNameContainingAndSupplierNameContainingAndPurchasePriceLessThanEqual(
//                        name != null ? name : "",
//                        supplierName != null ? supplierName : "",
//                        purchasePrice != null ? purchasePrice : Integer.MAX_VALUE,
//                        pageable);
//            }
//        }
//
//
//
//
//
//
//
//
//        public List<Product> findAllByIds(List<Long> ids) {
//            if (ids == null || ids.isEmpty()) {
//                return new ArrayList<>();
//            }
//            List<Integer> integerIds = ids.stream()
//                    .map(Long::intValue)
//                    .collect(Collectors.toList());
//            List<Product> products = productRepository.findAllById(integerIds);
//            return products;
//        }
//
//        public List<Product> searchProductToChoose(
//                String productName,
//                String supplierName,
//                String stockSort,
//                String priceSort,
//                String inStockStatus
//        ) {
//            return productRepository.searchProductToChoose(
//                    productName,
//                    supplierName,
//                    stockSort,
//                    priceSort,
//                    inStockStatus
//            );
//        }
//
//
//        @Override
//        public List<Product> findAllWithOutPageable() {
//            return productRepository.findAllWithOutPageable();
//        }
//
//
//        // Đình Anh
//        @Override
//        public void saveProduct(Product product) {
//            productRepository.saveProduct(product.getName(),
//                    product.getPurchasePrice(),
//                    product.getSellingPrice(),
//                    product.getRetailPrice(),
//                    product.getCPU(),
//                    product.getStorage(),
//                    product.getScreenSize(),
//                    product.getCamera(),
//                    product.getSelfie(),
//                    product.getDetailedDescription(),
//                    product.getStockQuantity(),
//                    product.getQrCode(),
//                    product.getSupplier().getSupplierID());
//
//        }
//
//        @Override
//        public void saveProductWithImg(Product product, List<MultipartFile> files) {
//            if (files != null && !files.isEmpty()) {
//                List<ProductImage> productImages = new ArrayList<>();
//                for (MultipartFile file : files) {
//                    if (file != null && !file.isEmpty()) {
//                        try {
//                            String imageUrl = cloudinaryService.uploadFile(file, "product");
//                            ProductImage pi = new ProductImage();
//                            pi.setImageUrl(imageUrl);
//                            pi.setProduct(product);
//                            productImages.add(pi);
//                        } catch (IOException e) {
//                            throw new RuntimeException("Lỗi khi upload ảnh sản phẩm", e);
//                        }
//                    }
//                }
//                // Gán danh sách ảnh đã tạo vào product
//                product.setImages(productImages);
//            }
//            // Lưu sản phẩm không có ảnh
//            System.out.println("Lưu sản phẩm");
//            productRepository.save(product);
//            System.out.println("Kết thúc lưu sản phẩm");
//        }
//
//        //update
//        @Override
//        public Product getProductById(Integer id) {
//            return productRepository.findByProductID(id);
//        }
//
//        @Override
//        public void updateProductWithSellingPrice(Product product) {
//            try{
//                productRepository.updateProduct(product.getProductID(),
//                        product.getName(),
//                        product.getSellingPrice(),
//                        product.getScreenSize(),
//                        product.getCamera(),
//                        product.getSelfie(),
//                        product.getCPU(),
//                        product.getStorage(),
//                        product.getDetailedDescription());
//            } catch (Exception e){
//                System.out.println("Lỗi khi thực hiện cập nhật: " + e);
//            }
//        }
//
//        @Override
//        public void saveProductImage(Product product, ProductImage productImage) {
//            productRepository.saveProductImage(productImage.getDescription(), productImage.getImageUrl(), product.getProductID());
//        }
//
//        @Override
//        public void deleteProductImages(Product product) {
//            productRepository.deleteProductImages(product.getProductID());
//        }
//
//        @Override
//        @Transactional
//        public void updateStockQuantity(Integer productId, int newStockQuantity) {
//            productRepository.updateStockQuantity(productId, newStockQuantity);
//        }
//
//        //Tuấn Anh
//        @Override
//        public Page<Product> findAll(Pageable pageable) {
//            return productRepository.findAll(pageable);
//        }
//
//        @Override
//        public List<Product> findAllProducts() {
//            return productRepository.findAllProducts();
//        }
//
//        @Override
//        public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, int purchasePrice, Pageable pageable) {
//            return productRepository.searchProductByNameAndSupplier_NameAndPurchasePrice(name,supplierName,purchasePrice,pageable);
//        }
//
//        // Đếm tổng số sản phẩm
//        @Override
//        public long countProducts() {
//            return productRepository.count();
//        }
//
//
//        @Override
//        public Integer countProductsHaveRetailPrice() {
//            return productRepository.countProductsHaveRetailPrice();
//        }
//
//
//
//        @Override
//        public Page<Product> searchProductsByKeyword(String keyword, Pageable pageable) {
//            if (keyword == null || keyword.trim().isEmpty()) {
//                return productRepository.findAll(pageable);
//            }
//            return productRepository.searchProductsByKeyword(keyword, pageable);
//        }
//
//        // Đếm tổng số sản phẩm đã bán
//    //    @Override
//    //    public Long countSoldProducts() {
//    //        return productRepository.countSoldProducts();
//    //    }
//
//        // Tính tổng doanh thu
//    //    @Override
//    //    public java.math.BigDecimal calculateTotalRevenue() {
//    //        return productRepository.calculateTotalRevenue();
//    //    }
//
//
//        @Override
//        public void save(Product product) {
//            productRepository.save(product);
//
//        }
//
//        @Override
//        public Product findById(Integer id) {
//            return productRepository.findByProductID(id);
//        }
//
//        @Override public Long countSoldProducts() { return productRepository.countSoldProducts(); }
//
//    }
package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.event.EntityChangeEvent;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import com.example.md5_phone_store_management.repository.IProductRepository;
import com.example.md5_phone_store_management.service.CloudinaryService;
import com.example.md5_phone_store_management.service.IProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService implements IProductService {

    @Autowired
    IProductRepository productRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePriceAndRetailPrice(
            String name, String supplierName, Integer purchasePrice, boolean noRetailPrice, Pageable pageable) {
        if (noRetailPrice) {
            return productRepository.findByNameContainingAndSupplierNameContainingAndPurchasePriceLessThanEqualAndRetailPriceIsNull(
                    name != null ? name : "",
                    supplierName != null ? supplierName : "",
                    purchasePrice != null ? purchasePrice : Integer.MAX_VALUE,
                    pageable);
        } else {
            return productRepository.findByNameContainingAndSupplierNameContainingAndPurchasePriceLessThanEqual(
                    name != null ? name : "",
                    supplierName != null ? supplierName : "",
                    purchasePrice != null ? purchasePrice : Integer.MAX_VALUE,
                    pageable);
        }
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> integerIds = ids.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
        return productRepository.findAllById(integerIds);
    }

    @Override
    public List<Product> searchProductToChoose(
            String productName, String supplierName, String stockSort, String priceSort, String inStockStatus) {
        return productRepository.searchProductToChoose(productName, supplierName, stockSort, priceSort, inStockStatus);
    }

    @Override
    public List<Product> findAllWithOutPageable() {
        return productRepository.findAllWithOutPageable();
    }

    @Override
    public void saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        String action = savedProduct.getRetailPrice() == null ? "INSERT_PW_NO_PRICE" : "INSERT_PW_PRICE";
        eventPublisher.publishEvent(new EntityChangeEvent(this, savedProduct, action, null));
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
            product.setImages(productImages);
        }
        Product savedProduct = productRepository.save(product);
        String action = savedProduct.getRetailPrice() == null ? "INSERT_PW_NO_PRICE" : "INSERT_PW_PRICE";
        eventPublisher.publishEvent(new EntityChangeEvent(this, savedProduct, action, null));
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findByProductID(id);
    }

    @Override
    public void updateProductWithSellingPrice(Product product) {
        Product existingProduct = productRepository.findByProductID(product.getProductID());
        if (existingProduct != null) {
            Product oldProduct = new Product();
            BeanUtils.copyProperties(existingProduct, oldProduct); // Copy old state
            try {
                productRepository.updateProduct(
                        product.getProductID(),
                        product.getName(),
                        product.getSellingPrice(),
                        product.getScreenSize(),
                        product.getCamera(),
                        product.getSelfie(),
                        product.getCPU(),
                        product.getStorage(),
                        product.getDetailedDescription());
                Product updatedProduct = productRepository.findByProductID(product.getProductID());
                // Kiểm tra nếu có thay đổi liên quan đến retailPrice
                if ((oldProduct.getRetailPrice() == null && updatedProduct.getRetailPrice() != null) ||
                        (oldProduct.getRetailPrice() != null && updatedProduct.getRetailPrice() == null) ||
                        (oldProduct.getRetailPrice() != null && !oldProduct.getRetailPrice().equals(updatedProduct.getRetailPrice()))) {
                    eventPublisher.publishEvent(new EntityChangeEvent(this, updatedProduct, "UPDATE_RETAIL_PRICE", oldProduct));
                } else {
                    eventPublisher.publishEvent(new EntityChangeEvent(this, updatedProduct, "UPDATE", oldProduct));
                }
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi thực hiện cập nhật: " + e);
            }
        } else {
            throw new RuntimeException("Product not found with ID: " + product.getProductID());
        }
    }

    @Override
    public void saveProductImage(Product product, ProductImage productImage) {
        productRepository.saveProductImage(productImage.getDescription(), productImage.getImageUrl(), product.getProductID());
    }

    @Override
    public void deleteProductImages(Product product) {
        Product existingProduct = productRepository.findByProductID(product.getProductID());
        if (existingProduct != null) {
            eventPublisher.publishEvent(new EntityChangeEvent(this, existingProduct, "DELETE_IMAGES", existingProduct));
            productRepository.deleteProductImages(product.getProductID());
        } else {
            throw new RuntimeException("Product not found with ID: " + product.getProductID());
        }
    }

    @Override
    @Transactional
    public void updateStockQuantity(Integer productId, int newStockQuantity) {
        Product existingProduct = productRepository.findByProductID(productId);
        if (existingProduct != null) {
            Product oldProduct = new Product();
            BeanUtils.copyProperties(existingProduct, oldProduct); // Copy old state
            productRepository.updateStockQuantity(productId, newStockQuantity);
            Product updatedProduct = productRepository.findByProductID(productId);
            eventPublisher.publishEvent(new EntityChangeEvent(this, updatedProduct, "UPDATE_STOCK", oldProduct));
        } else {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAllProducts();
    }

    @Override
    public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, int purchasePrice, Pageable pageable) {
        return productRepository.searchProductByNameAndSupplier_NameAndPurchasePrice(name, supplierName, purchasePrice, pageable);
    }

    @Override
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    public Integer countProductsHaveRetailPrice() {
        return productRepository.countProductsHaveRetailPrice();
    }

    @Override
    public Page<Product> searchProductsByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchProductsByKeyword(keyword, pageable);
    }

    @Override
    public void save(Product product) {
        Product savedProduct = productRepository.save(product);
        String action = savedProduct.getRetailPrice() == null ? "INSERT_PW_NO_PRICE" : "INSERT_PW_PRICE";
        eventPublisher.publishEvent(new EntityChangeEvent(this, savedProduct, action, null));
    }

    @Override
    public Product findById(Integer id) {
        return productRepository.findByProductID(id);
    }

    @Override
    public Long countSoldProducts() {
        return productRepository.countSoldProducts();
    }
}