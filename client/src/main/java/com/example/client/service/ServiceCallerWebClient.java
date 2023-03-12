package com.example.client.service;

import com.example.client.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ServiceCallerWebClient implements ServiceCaller{

    @Autowired
    private WebClient.Builder appWebClientBuilder;

    @Override
    public ProductResponse getProduct(String id) {
        return appWebClientBuilder.build().get()
                .uri("/api/product/" + id)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();
    }

    @Override
    public ArrayList<ProductResponse> getAllProduct() {
        return (ArrayList<ProductResponse>) appWebClientBuilder.build().get()
                .uri("/api/product")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<ProductResponse>>() {})
                .block();
    }

    @Override
    public Integer howManyInventory(String productId) {
        return appWebClientBuilder.build().get()
                .uri("/api/inventory/howManyInventory/" + productId)
                .retrieve()
                .bodyToMono(Integer.class)
                .block();
    }

    @Override
    public ArrayList<InventoryDto> getAllInventory() {
        return (ArrayList<InventoryDto>) appWebClientBuilder.build().get()
                .uri("/api/inventory/all")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<InventoryDto>>() {})
                .block();
    }



    @Override
    public UUID addProduct(ProductRequest productRequest) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        String productData =  productRequest.getProductName() + "," + productRequest.getDescription() +
              "," + productRequest.getPrice() + "," + productRequest.getQuantity();
        builder.part("product",productData);
        builder.part("image",productRequest.getImage().getResource());
        log.info("Call inventory-service to add inventory.");
        return  appWebClientBuilder.build().post().uri("/api/inventory/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(UUID.class)
                .block();
    }

    @Override
    public boolean addToShoppingCart(Integer quantity, String userId, ProductResponse productResponse) {
        OrderItemDto orderItemDto = OrderItemDto.builder().productId(productResponse.getId())
                .name(productResponse.getName()).price(productResponse.getPrice())
                .quantity(quantity).build();

        ShoppingCartDto shoppingCartDto = ShoppingCartDto.builder()
                .userid(UUID.fromString(userId))
                .orderItem(orderItemDto).build();

        return appWebClientBuilder.build().post()
                .uri("/api/order/addToShoppingCart")
                .header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(shoppingCartDto),ShoppingCartDto.class)
                .retrieve()
                .bodyToMono(boolean.class)
                .block();
    }

    @Override
    public ArrayList<OrderItemDto> getCartOrderItems(String userid) {
        return appWebClientBuilder.build().get()
                .uri("/api/order/cart",
                        uriBuilder -> uriBuilder.queryParam("userId",UUID.fromString(userid)).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<OrderItemDto>>() {})
                .block();
    }

    @Override
    public List<InventoryNotEnoughDto> placeOrder(String userid, @Nullable List<OrderItemDto> orderItems) {
       return appWebClientBuilder.build().post()
                .uri("/api/order/place",
                        uriBuilder -> uriBuilder.queryParam("userId",UUID.fromString(userid)).build())
               .headers(httpHeaders -> {
                   httpHeaders.setContentType(MediaType.APPLICATION_JSON);
               }) .body(Mono.just(orderItems),List.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InventoryNotEnoughDto>>() {
                })
                .block();
    }

    @Override
    public List<OrderDto> orderTrack(String userid) {
        return appWebClientBuilder.build().get()
                .uri("/api/order/orderTrack",
                        uriBuilder -> uriBuilder.queryParam("userId",UUID.fromString(userid)).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OrderDto>>() {})
                .block();
    }

    @Override
    public ArrayList<OrderItemDto> deleteCartItem(String userId, String productId) {
        return appWebClientBuilder.build().delete()
                .uri("/api/order/deleteCartItem",
                        uriBuilder -> uriBuilder
                                .queryParam("userId",userId)
                                .queryParam("productId", productId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<OrderItemDto>>() {})
                .block();
    }


}
