package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface IProductService {

    public List<Product> findAllByIds(List<Long> ids);

    Page<Product> searchProductByNameAndSupplier_NameAndPurchasePriceAndRetailPrice(
            String name, String supplierName, Integer purchasePrice, boolean noRetailPrice, Pageable pageable);


    List<Product> findAllWithOutPageable ();
    List<Product> searchProductToChoose(
            String productName,
            String supplierName,
            String stockSort,
            String priceSort,
            String inStockStatus
    );

    //Tuấn Anh
    Page<Product> findAll(Pageable pageable);
    List<Product> findAllProducts();
    //Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, double purchasePrice, Pageable pageable);
    Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(String name, String supplierName, int purchasePrice, Pageable pageable);

    //Đình Anh
    void saveProduct(Product product);
    void saveProductWithImg(Product product, List<MultipartFile> files);

    //update
    Product getProductById(Integer id);
    void updateProductWithSellingPrice(Product product);
    void saveProductImage(Product product, ProductImage productImage);
    void deleteProductImages(Product product);

    // Cập nhật số lượng tồn kho trực tiếp
    void updateStockQuantity(Integer productId, int newStockQuantity);


    // Đếm tổng số sản phẩm
    long countProducts();
    // Đếm tổng số sản phẩm đã bán
//    Long countSoldProducts();
    // Tính tổng doanh thu
//    BigDecimal calculateTotalRevenue();

    void save(Product product); // Thêm phương thức save

    Product findById(Integer id);

    Page<Product> searchProductsByKeyword(String keyword, Pageable pageable);

}