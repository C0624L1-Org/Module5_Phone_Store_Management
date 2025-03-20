package com.example.md5_phone_store_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Cart;
import com.example.md5_phone_store_management.model.CartItem;
import com.example.md5_phone_store_management.model.Product;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCart(Cart cart);
}
