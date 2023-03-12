package com.example.orderservice.persistence.repository;

import com.example.orderservice.persistence.entity.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, UUID> {
}
