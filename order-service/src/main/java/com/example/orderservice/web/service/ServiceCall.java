package com.example.orderservice.web.service;



import com.example.orderservice.web.dto.InventoryNotEnoughDto;
import com.example.orderservice.web.dto.OrderItemDto;

import java.util.List;
import java.util.UUID;

public interface ServiceCall {

    List<InventoryNotEnoughDto> checkInventory(String token, List<OrderItemDto> products);
    List<String> getProductImage(List<UUID> productIds);
}
