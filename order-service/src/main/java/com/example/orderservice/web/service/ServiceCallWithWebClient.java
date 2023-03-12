package com.example.orderservice.web.service;

import com.example.orderservice.web.dto.InventoryNotEnoughDto;
import com.example.orderservice.web.dto.OrderItemDto;
import com.example.orderservice.web.util.ExtractToken;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ServiceCallWithWebClient implements ServiceCall{

    @Autowired
    private WebClient.Builder appWebClientBuilder;

//    @Override
//    @CircuitBreaker(name = "id-one",fallbackMethod = "fallback")
//    @TimeLimiter(name = "id-one")
//    public CompletableFuture<List> checkInventory(String token, List<OrderItemDto> products) {
//
//        return CompletableFuture.supplyAsync(() ->appWebClientBuilder.build().get()
//                .uri("http://inventory-service/api/inventory/isAllInStock",
//                        uriBuilder -> uriBuilder.queryParam("productIds",products).build())
//                .headers(header -> header.setBearerAuth(token))
//                .retrieve()
//                .bodyToMono(List.class)
//                .block());
//
//    }

    @Override
    public List<InventoryNotEnoughDto> checkInventory(String token, List<OrderItemDto> products) {
        //  probably modify the quantity of inventory of products.
        return appWebClientBuilder.build().put()
                .uri("http://inventory-service/api/inventory/isAllInStock")
                .headers(header -> {
                    header.setBearerAuth(token);
                    header.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(Mono.just(products),List.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InventoryNotEnoughDto>>() {})
                .block();
    }

    @Override
    public List<String> getProductImage(List<UUID> productIds) {
        return appWebClientBuilder.build().get()
                .uri("http://product-service/api/product/images",
                        uriBuilder -> uriBuilder.queryParam("productIds",productIds).build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(ExtractToken.getToken()))
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }

//    private CompletableFuture<Boolean> fallback(List<String> skuCodes ,String token, CallNotPermittedException e){
//       throw e;
//    }
}
