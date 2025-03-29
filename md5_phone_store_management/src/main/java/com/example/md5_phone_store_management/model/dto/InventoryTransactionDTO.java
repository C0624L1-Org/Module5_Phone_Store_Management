package com.example.md5_phone_store_management.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransactionDTO {
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer productID;

    @NotNull(message = "Mã nhà cung cấp không được để trống")
    private Integer supplierID;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Giá nhập không được để trống")
    @Min(value = 1, message = "Giá nhập phải lớn hơn 0")
    private BigDecimal purchasePrice;

    @NotNull(message = "Nhân viên thực hiện không được để trống")
    private Integer employeeID ;

    private String transactionType = "IN"; // Mặc định là nhập kho
    private LocalDateTime transactionDate; // Thêm trường này
    // Getters và Setters
    public Integer getProductID() { return productID; }
    public void setProductID(Integer productID) { this.productID = productID; }
    public Integer getSupplierID() { return supplierID; }
    public void setSupplierID(Integer supplierID) { this.supplierID = supplierID; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public Integer getEmployeeID() { return employeeID; }
    public void setEmployeeID(Integer employeeID) { this.employeeID = employeeID; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}