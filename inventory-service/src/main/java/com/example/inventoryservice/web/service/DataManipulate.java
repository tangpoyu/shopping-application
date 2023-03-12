package com.example.inventoryservice.web.service;

import com.example.inventoryservice.persistence.model.Inventory;
import com.example.inventoryservice.web.dto.InventoryNotEnoughDto;
import com.example.inventoryservice.web.dto.OrderItemDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DataManipulate {

    Integer isInStock(UUID productId);
    List<InventoryNotEnoughDto> isAllInStock(List<OrderItemDto> products);
    List<Inventory> getAllInventory();

    UUID addInventory(String product, MultipartFile image);
}
