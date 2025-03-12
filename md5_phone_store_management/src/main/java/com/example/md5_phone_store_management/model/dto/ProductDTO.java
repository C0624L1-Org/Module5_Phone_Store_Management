package com.example.md5_phone_store_management.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductDTO {
    private Integer productID;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 5,message = "Tên sản phẩm phải có ít nhất 5 ký tự")
    @Size (max = 50,message = "Tên sản phẩm không vượt quá 50 ký tự")
    private String name;

    @NotNull(message = "Giá nhập không được để trống")
    @DecimalMin(value = "10000", message = "Giá nhập phải lớn hơn 10.000")
    private BigDecimal purchasePrice;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "10000", message = "Giá nhập phải lớn hơn 10.000")
    private BigDecimal sellingPrice;

    private String CPU;
    @Pattern(regexp = "^[1-9]\\d*(MB|GB|TB)$",
            message = "Bộ nhớ phải có định dạng số + MB, GB hoặc TB, ví dụ: 512MB, 8GB, 1TB")
    private String storage;
    private String screenSize;
    @Pattern(regexp = "^[1-9]\\d*MP$",
            message = "Camera phải có định dạng số + MP, ví dụ: 12MP, 108MP")
    private String camera;
    @Pattern(regexp = "^[1-9]\\d*MP$",
            message = "Camera phải có định dạng số + MP, ví dụ: 12MP, 108MP")
    private String selfie;
    private String detailedDescription;
    private String image;
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Positive(message = "Số lượng tồn kho phải lớn hơn 0")
    private Integer stockQuantity;
    private String qrCode;
    private Integer supplierID;

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getCPU() {
        return CPU;
    }

    public void setCPU(String CPU) {
        this.CPU = CPU;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getSelfie() {
        return selfie;
    }

    public void setSelfie(String selfie) {
        this.selfie = selfie;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
    }
}
