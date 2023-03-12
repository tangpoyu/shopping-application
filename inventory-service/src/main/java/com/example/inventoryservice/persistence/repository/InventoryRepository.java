package com.example.inventoryservice.persistence.repository;

import com.example.inventoryservice.persistence.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<List<Inventory>> findByProductIdIn(List<UUID> productId);
}
