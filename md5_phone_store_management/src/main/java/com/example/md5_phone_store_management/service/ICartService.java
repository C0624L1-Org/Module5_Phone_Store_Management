package com.example.md5_phone_store_management.service;

import com.example.md5_phone_store_management.model.Cart;
import com.example.md5_phone_store_management.model.CartItem;
import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ICartService {
    // Cart operations
    Cart getActiveCartForCustomer(Customer customer);
    Cart createCart(Customer customer);
    Cart getCartById(Integer cartId);
    void saveCart(Cart cart);
    void deleteCart(Cart cart);

    // Cart item operations
    CartItem addItemToCart(Cart cart, Product product, int quantity);
    void removeItemFromCart(Cart cart, Product product);
    void updateItemQuantity(Cart cart, Product product, int quantity);
    void clearCart(Cart cart);

    // Cart calculations
    BigDecimal getCartTotal(Cart cart);
    int getCartItemCount(Cart cart);
    List<CartItem> getCartItems(Cart cart);

    // Process cart
    void markCartAsInactive(Cart cart);
}
