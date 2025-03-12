package com.example.md5_phone_store_management.repository;

import com.example.md5_phone_store_management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public interface IProductRepository extends JpaRepository<Product, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO product (name, purchasePrice, sellingPrice, CPU, storage, " +
            "screenSize, camera, selfie, detailedDescription, image, stockQuantity," +
            " qrCode, supplierID) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13)", nativeQuery = true)
    void saveProduct(String name, BigDecimal purchasePrice, BigDecimal sellingPrice, String CPU,
                     String storage, String screenSize, String camera, String selfie,
                     String detailedDescription, String image, Integer stockQuantity,
                     String qrCode, Integer supplierID);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.name = :name, p.purchasePrice = :purchasePrice,p.sellingPrice=:sellingPrice, p.image = :image, " +
            "p.screenSize = :screenSize, p.camera = :camera, p.selfie = :selfie, " +
            "p.CPU = :cpu, p.storage = :storage, p.detailedDescription = :description " +
            "WHERE p.productID = :productID")
    int updateProductInfo(@Param("productID") Integer productID,
                          @Param("name") String name,
                          @Param("purchasePrice") BigDecimal purchasePrice,
                          @Param("sellingPrice") BigDecimal sellingPrice,
                          @Param("image") String image,
                          @Param("screenSize") String screenSize,
                          @Param("camera") String camera,
                          @Param("selfie") String selfie,
                          @Param("cpu") String cpu,
                          @Param("storage") String storage,
                          @Param("description") String description);
}
