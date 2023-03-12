package com.example.orderservice.web.service;

import com.example.orderservice.persistence.entity.ShoppingCartItem;
import com.example.orderservice.web.dto.OrderDto;
import com.example.orderservice.web.dto.OrderItemDto;
import com.example.orderservice.web.dto.ShoppingCartDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface DataManipulate {

    boolean addToShoppingCart(ShoppingCartDto shoppingCartDto);

    ArrayList<OrderItemDto> getCart(UUID userid);

    void placeOrder(UUID userId, List<OrderItemDto> orderRequest);

    void resetCart(UUID userId, List<OrderItemDto> orderItemDtos);

    List<OrderDto> getAllOrder(UUID uuid);

    void deleteCartItem(String userId, String productId);
}
