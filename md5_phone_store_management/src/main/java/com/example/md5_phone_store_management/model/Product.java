package com.example.md5_phone_store_management.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productID;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "decimal(15,0)")
    private BigDecimal purchasePrice;
    @Column(nullable = false, columnDefinition = "decimal(15,0)")
    private BigDecimal sellingPrice;
    @Column(columnDefinition = "decimal(15,0)")
    private BigDecimal retailPrice;
    private String CPU;
    private String storage;
    private String screenSize;
    private String camera;
    private String selfie;

    @Lob
    private String detailedDescription;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> invoiceDetails;

    private Integer stockQuantity;

    private String qrCode;

    @ManyToOne
    @JoinColumn(name = "supplierID", foreignKey = @ForeignKey(name = "FK_product_supplier"))
    private Supplier supplier;



    public Product() {
    }

    public Product(Integer productID, String name, BigDecimal purchasePrice, BigDecimal sellingPrice, String CPU, String storage, String screenSize, String camera, String selfie, String detailedDescription, List<ProductImage> images, Integer stockQuantity, String qrCode, Supplier supplier) {
        this.productID = productID;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.CPU = CPU;
        this.storage = storage;
        this.screenSize = screenSize;
        this.camera = camera;
        this.selfie = selfie;
        this.detailedDescription = detailedDescription;
        this.images = images;
        this.stockQuantity = stockQuantity;
        this.qrCode = qrCode;
        this.supplier = supplier;
    }

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

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
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

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", name='" + name + '\'' +
                ", purchasePrice=" + purchasePrice +
                ", sellingPrice=" + sellingPrice +
                ", retailPrice=" + retailPrice +
                ", CPU='" + CPU + '\'' +
                ", storage='" + storage + '\'' +
                ", screenSize='" + screenSize + '\'' +
                ", camera='" + camera + '\'' +
                ", selfie='" + selfie + '\'' +
                ", detailedDescription='" + (detailedDescription != null ? detailedDescription.substring(0, Math.min(detailedDescription.length(), 50)) + "..." : null) + '\'' +
                ", imagesCount=" + (images != null ? images.size() : 0) +
                ", stockQuantity=" + stockQuantity +
                ", qrCode='" + qrCode + '\'' +
                ", supplier=" + (supplier != null ? supplier.getSupplierID() : null) +
                '}';
    }
}
