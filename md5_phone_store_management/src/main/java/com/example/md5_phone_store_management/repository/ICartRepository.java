package com.example.md5_phone_store_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.md5_phone_store_management.model.Cart;
import com.example.md5_phone_store_management.model.Customer;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByCustomerAndIsActiveTrue(Customer customer);
    Optional<Cart> findByIdAndIsActiveTrue(Integer id);
}
