package com.example.md5_phone_store_management.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private Boolean isActive = true;

    public Cart() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Cart(Customer customer) {
//        this.customer = customer;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public void addItem(Product product, int quantity) {
        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        for (CartItem item : items) {
            if (item.getProduct().getProductID().equals(product.getProductID())) {
                // Cập nhật số lượng
                item.setQuantity(item.getQuantity() + quantity);
                this.updatedAt = LocalDateTime.now();
                return;
            }
        }

        // Add new item
        CartItem newItem = new CartItem(this, product, quantity);
        items.add(newItem);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getProductID().equals(product.getProductID()));
        this.updatedAt = LocalDateTime.now();
    }

    public void updateItemQuantity(Product product, int quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getProductID().equals(product.getProductID())) {
                item.setQuantity(quantity);
                this.updatedAt = LocalDateTime.now();
                return;
            }
        }
    }

    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItemsCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getFullName() : "guest") +
                ", itemsCount=" + items.size() +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
}
