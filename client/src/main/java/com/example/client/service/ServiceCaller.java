package com.example.client.service;

import com.example.client.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface ServiceCaller {


    UUID addProduct(ProductRequest productRequest);


    ProductResponse getProduct(String id);


    ArrayList<ProductResponse> getAllProduct();


    Integer howManyInventory(String productId);


    ArrayList<InventoryDto> getAllInventory();


    boolean addToShoppingCart(Integer quantity, String userId, ProductResponse productResponse);


    ArrayList<OrderItemDto> getCartOrderItems(String userid);


    List<InventoryNotEnoughDto> placeOrder(String userid, List<OrderItemDto> orderItems);


    List<OrderDto> orderTrack(String userid);

    ArrayList<OrderItemDto> deleteCartItem(String userId, String productId);
}

