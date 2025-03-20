package com.example.md5_phone_store_management.service.implement;

import com.example.md5_phone_store_management.model.Cart;
import com.example.md5_phone_store_management.model.CartItem;
import com.example.md5_phone_store_management.model.Customer;
import com.example.md5_phone_store_management.model.Product;
import com.example.md5_phone_store_management.repository.ICartItemRepository;
import com.example.md5_phone_store_management.repository.ICartRepository;
import com.example.md5_phone_store_management.service.ICartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService implements ICartService {
    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private ICartItemRepository cartItemRepository;


    @Override
    public Cart getActiveCartForCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }

        return cartRepository.findByCustomerAndIsActiveTrue(customer)
                .orElseGet(() -> createCart(customer));
    }

    @Override
    public Cart createCart(Customer customer) {
        Cart cart = new Cart(customer);
        cart.setIsActive(true);
        return cartRepository.save(cart);
    }

    @Override
    public Cart getCartById(Integer cartId) {
        return cartRepository.findByIdAndIsActiveTrue(cartId).orElse(null);
    }

    @Override
    public void saveCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void deleteCart(Cart cart) {
        cartItemRepository.deleteByCart(cart);
        cartRepository.delete(cart);
    }


    @Override
    @Transactional
    public CartItem addItemToCart(Cart cart, Product product, int quantity) {
        if (cart == null || product == null || quantity <= 0) {
            return null;
        }

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);

            // Update cart last modified
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);

            return cartItem;
        } else {
            // Create new cart item
            CartItem newItem = new CartItem(cart, product, quantity);
            CartItem savedItem = cartItemRepository.save(newItem);

            // Update cart last modified
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);

            return savedItem;
        }
    }

    @Override
    @Transactional
    public void removeItemFromCart(Cart cart, Product product) {
        if (cart == null || product == null) {
            return;
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        existingItem.ifPresent(cartItem -> {
            cartItemRepository.delete(cartItem);

            // Update cart last modified
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void updateItemQuantity(Cart cart, Product product, int quantity) {
        if (cart == null || product == null || quantity <= 0) {
            return;
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        existingItem.ifPresent(cartItem -> {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);

            // Update cart last modified
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public void clearCart(Cart cart) {
        if (cart == null) {
            return;
        }

        cartItemRepository.deleteByCart(cart);

        // Update cart last modified
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }


    @Override
    public BigDecimal getCartTotal(Cart cart) {
        if (cart == null) {
            return BigDecimal.ZERO;
        }

        List<CartItem> items = cartItemRepository.findByCart(cart);
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getCartItemCount(Cart cart) {
        if (cart == null) {
            return 0;
        }

        List<CartItem> items = cartItemRepository.findByCart(cart);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    public List<CartItem> getCartItems(Cart cart) {
        if (cart == null) {
            return List.of();
        }

        return cartItemRepository.findByCart(cart);
    }

    @Override
    @Transactional
    public void markCartAsInactive(Cart cart) {
        if (cart == null) {
            return;
        }

        cart.setIsActive(false);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
