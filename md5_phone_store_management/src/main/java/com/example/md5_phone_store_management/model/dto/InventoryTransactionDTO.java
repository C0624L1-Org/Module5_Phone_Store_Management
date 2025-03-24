package com.example.md5_phone_store_management.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransactionDTO {

        private Integer transactionID;

        @NotNull(message = "Product không được để trống")
        private Integer productID;

        @NotNull(message = "Loại giao dịch không được để trống")
        private String transactionType;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;

        @DecimalMin(value = "0.00", inclusive = false, message = "Giá mua phải lớn hơn 0")
        @Digits(integer = 10, fraction = 2, message = "Giá mua phải có tối đa 10 chữ số nguyên và 2 chữ số thập phân")
        private BigDecimal purchasePrice;

        private LocalDateTime transactionDate = LocalDateTime.now();

        @NotNull(message = "Nhà cung cấp không được để trống")
        private Integer supplierID;

        @NotNull(message = "Nhân viên thực hiện không được để trống")
        private Integer employeeID;

        @DecimalMin(value = "0.00", message = "Tổng giá trị không được nhỏ hơn 0")
        @Digits(integer = 12, fraction = 2, message = "Tổng giá trị phải có tối đa 12 chữ số nguyên và 2 chữ số thập phân")
        private BigDecimal totalPrice;

        public Integer getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(Integer transactionID) {
            this.transactionID = transactionID;
        }

        public Integer getProductID() {
            return productID;
        }

        public void setProductID(Integer productID) {
            this.productID = productID;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPurchasePrice() {
            return purchasePrice;
        }

        public void setPurchasePrice(BigDecimal purchasePrice) {
            this.purchasePrice = purchasePrice;
        }

        public LocalDateTime getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
        }

        public Integer getSupplierID() {
            return supplierID;
        }

        public void setSupplierID(Integer supplierID) {
            this.supplierID = supplierID;
        }

        public Integer getEmployeeID() {
            return employeeID;
        }

        public void setEmployeeID(Integer employeeID) {
            this.employeeID = employeeID;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
}
