//package com.example.md5_phone_store_management.service.implement;
//
//import com.example.md5_phone_store_management.event.EntityChangeEvent;
//import com.example.md5_phone_store_management.model.Product;
//import com.example.md5_phone_store_management.model.ProductImage;
//import com.example.md5_phone_store_management.repository.IProductRepository;
//import com.example.md5_phone_store_management.service.CloudinaryService;
//import com.example.md5_phone_store_management.service.IProductService;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class ProductService implements IProductService {
//
//    @Autowired
//    IProductRepository productRepository;
//
//    @Autowired
//    CloudinaryService cloudinaryService;
//
//    @Autowired
//    private ApplicationEventPublisher eventPublisher;
//
//    @Override
//    public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePriceAndRetailPrice(
//            String name, String supplierName, Integer purchasePrice, boolean noRetailPrice, Pageable pageable) {
//        if (noRetailPrice) {
//            return productRepository.findByNameContainingAndSupplierNameContainingAndPurchasePriceLessThanEqualAndRetailPriceIsNull(
//                    name != null ? name : "",
//                    supplierName != null ? supplierName : "",
//                    purchasePrice != null ? purchasePrice : Integer.MAX_VALUE,
//                    pageable);
//        } else {
//            return productRepository.findByNameContainingAndSupplierNameContainingAndPurchasePriceLessThanEqual(
//                    name != null ? name : "",
//                    supplierName != null ? supplierName : "",
//                    purchasePrice != null ? purchasePrice : Integer.MAX_VALUE,
//                    pageable);
//        }
//    }
//
//    @Override
//    public List<Product> findAllByIds(List<Long> ids) {
//        if (ids == null || ids.isEmpty()) {
//            return new ArrayList<>();
//        }
//        List<Integer> integerIds = ids.stream()
//                .map(Long::intValue)
//                .collect(Collectors.toList());
//        return productRepository.findAllById(integerIds);
//    }
//
//    @Override
//    public List<Product> searchProductToChoose(
//            String productName, String supplierName, String stockSort, String priceSort, String inStockStatus) {
//        return productRepository.searchProductToChoose(productName, supplierName, stockSort, priceSort, inStockStatus);
//    }
//
//    @Override
//    public List<Product> findAllWithOutPageable() {
//        return productRepository.findAllWithOutPageable();
//    }
//
//    @Override
//    public void saveProduct(Product product) {
//        Product savedProduct = productRepository.save(product);
//        String action = savedProduct.getRetailPrice() == null ? "INSERT_PW_NO_PRICE" : "INSERT_PW_PRICE";
//        eventPublisher.publishEvent(new EntityChangeEvent(this, savedProduct, action, null));
//    }
//
//    @Override
//    public void saveProductWithImg(Product product, List<MultipartFile> files) {
//        if (files != null && !files.isEmpty()) {
//            List<ProductImage> productImages = new ArrayList<>();
//            for (MultipartFile file : files) {
//                if (file != null && !file.isEmpty()) {
//                    try {
//                        String imageUrl = cloudinaryService.uploadFile(file, "product");
//                        ProductImage pi = new ProductImage();
//                        pi.setImageUrl(imageUrl);
//                        pi.setProduct(product);
//                        productImages.add(pi);
//                    } catch (IOException e) {
//                        throw new RuntimeException("Lỗi khi upload ảnh sản phẩm", e);
//                    }
//                }
//            }
//            product.setImages(productImages);
//        }
//        Product savedProduct = productRepository.save(product);
//        String action = savedProduct.getRetailPrice() == null ? "INSERT_PW_NO_PRICE" : "INSERT_PW_PRICE";
//        eventPublisher.publishEvent(new EntityChangeEvent(this, savedProduct, action, null));
//    }
//
//    @Override
//    public Product getProductById(Integer id) {
//        return productRepository.findByProductID(id);
//    }
//
//    @Override
//    public void updateProductWithSellingPrice(Product product) {
//        Product existingProduct = productRepository.findByProductID(product.getProductID());
//        if (existingProduct != null) {
//            Product oldProduct = new Product();
//            BeanUtils.copyProperties(existingProduct, oldProduct); // Copy old state
//            try {
//                productRepository.updateProduct(
//                        product.getProductID(),
//                        product.getName(),
//                        product.getSellingPrice(),
//                        product.getScreenSize(),
//                        product.getCamera(),
//                        product.getSelfie(),
//                        product.getCPU(),
//                        product.getStorage(),
//                        product.getDetailedDescription());
//                Product updatedProduct = productRepository.findByProductID(product.getProductID());
//                // Kiểm tra nếu có thay đổi liên quan đến retailPrice
//                if ((oldProduct.getRetailPrice() == null && updatedProduct.getRetailPrice() != null) ||
//                        (oldProduct.getRetailPrice() != null && updatedProduct.getRetailPrice() == null) ||
//                        (oldProduct.getRetailPrice() != null && !oldProduct.getRetailPrice().equals(updatedProduct.getRetailPrice()))) {
//                    eventPublisher.publishEvent(new EntityChangeEvent(this, updatedProduct, "UPDATE_RETAIL_PRICE", oldProduct));
//                } else {
//                    eventPublisher.publishEvent(new EntityChangeEvent(this, updatedProduct, "UPDATE", oldProduct));
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("Lỗi khi thực hiện cập nhật: " + e);
//            }
//        } else {
//            throw new RuntimeException("Product not found with ID: " + product.getProductID());
//        }
//    }
//
//    @Override
//    public void saveProductImage(Product product, ProductImage productImage) {
//        productRepository.saveProductImage(productImage.getDescription(), productImage.getImageUrl(), product.getProductID());
//    }
//
//    @Override
//    public void deleteProductImages(Product product) {
//        Product existingProduct = productRepository.findByProductID(product.getProductID());
//        if (existingProduct != null) {
//            eventPublisher.publishEvent(new EntityChangeEvent(this, existingProduct, "DELETE_IMAGES", existingProduct));
//            productRepository.deleteProductImages(product.getProductID());
//        } else {
//            throw new RuntimeException("Product not found with ID: " + product.getProductID());
//        }
//    }
//
//    @Override
//    @Transactional
//    public void updateStockQuantity(Integer productId, int newStockQuantity) {
//        Product existingProduct = productRepository.findByProductID(productId);
//        if (existingProduct != null) {
//            Product oldProduct = new Product();
//            BeanUtils.copyProperties(existingProduct, oldProduct); // Copy old state
//            productRepository.updateStockQuantity(productId, newStockQuantity);
//            Product updatedProduct = productRepository.findByProductID(productId);
//            eventPublisher.publishEvent(new EntityChangeEvent(this, updatedProduct, "UPDATE_STOCK", oldProduct));
//        } else {
//            throw new RuntimeException("Product not found with ID: " + productId);
//        }
//    }
//
//    @Override
//    public Page<Product> findAll(Pageable pageable) {
//        return productRepository.findAll(pageable);
//    }
//
//    @Override
//    public List<Product> findAllProducts() {
//        return productRepository.findAllProducts();
//    }
//
//    @Override
//    public Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, int purchasePrice, Pageable pageable) {
//        return productRepository.searchProductByNameAndSupplier_NameAndPurchasePrice(name, supplierName, purchasePrice, pageable);
//    }
//
//    @Override
//    public long countProducts() {
//        return productRepository.count();
//    }
//
//    @Override
//    public Integer countProductsHaveRetailPrice() {
//        return productRepository.countProductsHaveRetailPrice();
//    }
//
//    @Override
//    public Page<Product> searchProductsByKeyword(String keyword, Pageable pageable) {
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return productRepository.findAll(pageable);
//        }
//        return productRepository.searchProductsByKeyword(keyword, pageable);
//    }
//
//    @Override
//    public void save(Product product) {
//        Product savedProduct = productRepository.save(product);
//        String action = savedProduct.getRetailPrice() == null ? "INSERT_PW_NO_PRICE" : "INSERT_PW_PRICE";
//        eventPublisher.publishEvent(new EntityChangeEvent(this, savedProduct, action, null));
//    }
//
//    @Override
//    public Product findById(Integer id) {
//        return productRepository.findByProductID(id);
//    }
//
//    @Override
//    public Long countSoldProducts() {
//        return productRepository.countSoldProducts();
//    }
//}
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
        String newValue = String.format("productID=%d, retailPrice=%s",
                savedProduct.getProductID(),
                savedProduct.getRetailPrice() != null ? savedProduct.getRetailPrice() : 0);
        EntityChangeEvent event = new EntityChangeEvent(this, savedProduct, action, null);
        event.addMetadata("newValue", newValue);
        eventPublisher.publishEvent(event);
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
        String newValue = String.format("productID=%d, retailPrice=%s",
                savedProduct.getProductID(),
                savedProduct.getRetailPrice() != null ? savedProduct.getRetailPrice() : 0);
        EntityChangeEvent event = new EntityChangeEvent(this, savedProduct, action, null);
        event.addMetadata("newValue", newValue);
        eventPublisher.publishEvent(event);
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
                // Check if retailPrice has changed
                if ((oldProduct.getRetailPrice() == null && updatedProduct.getRetailPrice() != null) ||
                        (oldProduct.getRetailPrice() != null && updatedProduct.getRetailPrice() == null) ||
                        (oldProduct.getRetailPrice() != null && !oldProduct.getRetailPrice().equals(updatedProduct.getRetailPrice()))) {
                    String oldValue = String.format("productID=%d, retailPrice=%s",
                            oldProduct.getProductID(),
                            oldProduct.getRetailPrice() != null ? oldProduct.getRetailPrice() : 0);
                    String newValue = String.format("productID=%d, retailPrice=%s",
                            updatedProduct.getProductID(),
                            updatedProduct.getRetailPrice() != null ? updatedProduct.getRetailPrice() : 0);
                    EntityChangeEvent event = new EntityChangeEvent(this, updatedProduct, "UPDATE_RETAIL_PRICE", oldProduct);
                    event.addMetadata("newValue", newValue);
                    event.addMetadata("oldValue", oldValue);
                    eventPublisher.publishEvent(event);
                } else {
                    // Include product name in metadata for UPDATE action
                    String newValue = String.format("productID=%d, name=%s",
                            updatedProduct.getProductID(),
                            updatedProduct.getName() != null ? updatedProduct.getName() : "Unknown");
                    String oldValue = String.format("productID=%d, name=%s",
                            oldProduct.getProductID(),
                            oldProduct.getName() != null ? oldProduct.getName() : "Unknown");
                    EntityChangeEvent event = new EntityChangeEvent(this, updatedProduct, "UPDATE", oldProduct);
                    event.addMetadata("newValue", newValue);
                    event.addMetadata("oldValue", oldValue);
                    eventPublisher.publishEvent(event);
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
            EntityChangeEvent event = new EntityChangeEvent(this, existingProduct, "DELETE_IMAGES", existingProduct);
            eventPublisher.publishEvent(event);
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
            EntityChangeEvent event = new EntityChangeEvent(this, updatedProduct, "UPDATE_STOCK", oldProduct);
            eventPublisher.publishEvent(event);
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
        String newValue = String.format("productID=%d, retailPrice=%s",
                savedProduct.getProductID(),
                savedProduct.getRetailPrice() != null ? savedProduct.getRetailPrice() : 0);
        EntityChangeEvent event = new EntityChangeEvent(this, savedProduct, action, null);
        event.addMetadata("newValue", newValue);
        eventPublisher.publishEvent(event);
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