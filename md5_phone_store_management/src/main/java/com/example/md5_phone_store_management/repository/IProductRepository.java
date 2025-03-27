package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.model.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Integer> {


    @Query("SELECT p FROM Product p " +
            "WHERE (:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:supplierName IS NULL OR LOWER(p.supplier.name) LIKE LOWER(CONCAT('%', :supplierName, '%'))) " +
            "AND (:inStockStatus IS NULL " +
            "     OR (:inStockStatus = 'inStock' AND p.stockQuantity > 0) " +
            "     OR (:inStockStatus = 'outStock' AND p.stockQuantity = 0)) " +
            "ORDER BY " +
            "CASE WHEN :stockSort = 'ASC' THEN p.stockQuantity END ASC, " +
            "CASE WHEN :stockSort = 'DESC' THEN p.stockQuantity END DESC, " +
            "CASE WHEN :priceSort = 'ASC' THEN p.sellingPrice END ASC, " +
            "CASE WHEN :priceSort = 'DESC' THEN p.sellingPrice END DESC")
    List<Product> searchProductToChoose(
            @Param("productName") String productName,
            @Param("supplierName") String supplierName,
            @Param("stockSort") String stockSort,
            @Param("priceSort") String priceSort,
            @Param("inStockStatus") String inStockStatus
    );




    // Đình Anh: Sửa lại truy vấn INSERT, bỏ trường image
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO product (name, purchasePrice, sellingPrice, retailPrice, CPU, storage, " +
            "screenSize, camera, selfie, detailedDescription, stockQuantity, " +
            "qrCode, supplierID) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13)", nativeQuery = true)
    void saveProduct(String name, BigDecimal purchasePrice, BigDecimal sellingPrice, BigDecimal retailPrice, String CPU,
                     String storage, String screenSize, String camera, String selfie,
                     String detailedDescription, Integer stockQuantity,
                     String qrCode, Integer supplierID);


    // Tuấn Anh
    @Query("SELECT p FROM Product p")
    Page<Product> findAll(Pageable pageable);

    @Query("select p from Product p where " +
            "(:name is null or p.name like concat('%',:name,'%')) " +
            "and (:supplierName is null or p.supplier.name like concat('%',:supplierName,'%')) " +
            "and (:purchasePrice = 0 or p.purchasePrice <= :purchasePrice)")
    Page<Product> searchProductByNameAndSupplier_NameAndPurchasePrice(@Param("name") String name,
                                                                      @Param("supplierName") String supplierName,
                                                                      @Param("purchasePrice") int purchasePrice,
                                                                      Pageable pageable);

    @Query("SELECT p FROM Product p")
    List<Product> findAllWithOutPageable();



    //update
    @Query(value = "SELECT * FROM product WHERE product.productID = :id", nativeQuery = true)
    Product findByProductID(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE product p SET p.name = ?2, p.sellingPrice = ?3, p.screenSize = ?4, " +
            "p.camera = ?5, p.selfie = ?6, p.CPU = ?7, p.storage = ?8, p.detailedDescription = ?9 " +
            "WHERE p.productID = ?1", nativeQuery = true)
    void updateProduct(Integer productID,
                          String name,
                          BigDecimal sellingPrice,
                          String screenSize,
                          String camera,
                          String selfie,
                          String cpu,
                          String storage,
                          String description);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO product_images(description, imageUrl, product_id) VALUES (?1, ?2, ?3)", nativeQuery = true)
    void saveProductImage(String description, String imageUrl, Integer productID);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM product_images WHERE product_id = ?1", nativeQuery = true)
    void deleteProductImages(Integer productID);

    // Đếm tổng số sản phẩm
    @Query(value = "SELECT COUNT(*) FROM product", nativeQuery = true)
    long countProducts();




    // Đếm tổng số sản phẩm đã bán
//    @Query(value = "SELECT SUM(quantity) FROM invoice_detail", nativeQuery = true)
//    Long countSoldProducts();

    // Tính tổng doanh thu
//    @Query(value = "SELECT SUM(total_amount) FROM invoice", nativeQuery = true)
//    BigDecimal calculateTotalRevenue();

}
