package com.example.md5_phone_store_management.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventoryTransaction")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionID;

    @ManyToOne
    @JoinColumn(name = "productID", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime transactionDate ;

    @ManyToOne
    @JoinColumn(name = "supplierID", foreignKey = @ForeignKey(name = "FK_Supplier"))
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "employeeID", foreignKey = @ForeignKey(name = "FK_Employee"))
    private Employee employee;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // Getters and Setters
    @PrePersist
    @PreUpdate
    private void updateValuesFromProduct() {
        if (product != null) {
            this.quantity = product.getStockQuantity();
            this.purchasePrice = product.getPurchasePrice();
            this.totalPrice = purchasePrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Getters and Setters
    public Integer getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Integer transactionID) {
        this.transactionID = transactionID;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        updateValuesFromProduct();  // Gọi lại khi thay đổi product
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPurchasePrice(BigDecimal price) {
        this.purchasePrice = price;
    }
}
