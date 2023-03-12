package com.example.orderservice.web.controller;

import com.example.orderservice.web.dto.InventoryNotEnoughDto;
import com.example.orderservice.web.dto.OrderDto;
import com.example.orderservice.web.dto.OrderItemDto;
import com.example.orderservice.web.dto.ShoppingCartDto;
import com.example.orderservice.web.service.DataManipulate;
import com.example.orderservice.web.service.ServiceCall;
import com.example.orderservice.web.util.ExtractToken;
import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private DataManipulate dataManipulate;

    @Autowired
    private ServiceCall serviceCall;

    @GetMapping("/cart")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('user')")
//    @Observed(name = "get.cart",contextualName = "getCart")
    public ArrayList<OrderItemDto> getCart(@RequestParam("userId") UUID userid){
        return dataManipulate.getCart(userid);
    }

    @PostMapping("/addToShoppingCart")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('user')")
//    @Observed(name = "add.to.cart",contextualName = "addToShoppingCart")
    public boolean addToShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto){
        boolean result = dataManipulate.addToShoppingCart(shoppingCartDto);
        return result;
    }

    @DeleteMapping("/deleteCartItem")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('user')")
    public ArrayList<OrderItemDto> deleteCartItem(@RequestParam("userId") String userId,
                                                  @RequestParam("productId") String productId){
        dataManipulate.deleteCartItem(userId,productId);
        return dataManipulate.getCart(UUID.fromString(userId));
    }

    @PostMapping("/place")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
//    @Observed(name = "place.order",contextualName = "placeOrder")
    public List<InventoryNotEnoughDto> placeOrder(@RequestParam("userId") UUID userId, @RequestBody List<OrderItemDto> orderItemDtos){
        List<InventoryNotEnoughDto> list = serviceCall.checkInventory(ExtractToken.getToken(), orderItemDtos);
        if(list.isEmpty()){
            // inventory is enough, place an order and reset shopping cart.
            dataManipulate.placeOrder(userId,orderItemDtos);
            dataManipulate.resetCart(userId,orderItemDtos);
            return new ArrayList<>();
        }else {
           // inventory isn't enough, return List to client to tell customer which product quantity needs to be modified.
            return list;
        }
    }

    @GetMapping("/orderTrack")
    @ResponseStatus(HttpStatus.OK)
//    @Observed(name = "order.track",contextualName = "orderTrack")
    public List<OrderDto> orderTrack(@RequestParam("userId") UUID uuid){
        return dataManipulate.getAllOrder(uuid);
    }

//    @PostMapping("/place")
//    @ResponseStatus(HttpStatus.CREATED)
//    public CompletionStage<Boolean> placeOrder(@RequestParam("userId") UUID userId){
//        List<OrderItemDto> products = dataManipulate.getCart(userId);
//
//        Supplier<CompletionStage<List>> inInStock = () -> serviceCall.checkInventory(ExtractToken.getToken(), products);
//
//        return inInStock.get().whenComplete((result,ex) -> {
//                if (ex != null) {
//                    System.out.println(ex.getMessage());
//                }
//                if (result != null) {
//                    if(result.isEmpty()){
//                        // inventory is enough, place an order and reset shopping cart.
//                        dataManipulate.placeOrder();
//                    }else {
//                        // inventory isn't enough, return List to client to tell customer which product quantity needs to be modified.
//                        throw new IllegalArgumentException("Product is run out of stock.");
//                    }
//                }
//            }
//        );
//    }


//    @GetMapping
//    @ResponseStatus(HttpStatus.OK)
//    public boolean hi(){
//        ArrayList skus = new ArrayList();
//        skus.add("1");
//        skus.add("2");
//        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return appWebClientBuilder.build().get()
//                .uri("http://inventory-service/api/inventory",
//                        uriBuilder -> uriBuilder.queryParam("skuCodes",skus).build())
//                .headers(header -> header.setBearerAuth(jwt.getTokenValue()))
//                .retrieve()
//                .bodyToMono(boolean.class)
//                .block(); // make webclient call
//    }
}
