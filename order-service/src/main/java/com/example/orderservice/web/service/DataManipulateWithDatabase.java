package com.example.orderservice.web.service;

import com.example.orderservice.persistence.entity.*;
import com.example.orderservice.persistence.repository.OrderRepository;
import com.example.orderservice.persistence.repository.ShoppingCartItemRepository;
import com.example.orderservice.persistence.repository.ShoppingCartRepository;
import com.example.orderservice.web.dto.OrderDto;
import com.example.orderservice.web.dto.OrderItemDto;
import com.example.orderservice.web.dto.ShoppingCartDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataManipulateWithDatabase implements DataManipulate{

    @Autowired
    private ServiceCall serviceCall;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ShoppingCartItemRepository shoppingCartItemRepository;

    @Override
    @Transactional
    public boolean addToShoppingCart(ShoppingCartDto shoppingCartDto) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartDto.getUserid()).orElse(null);

        if(shoppingCart == null){
            log.info("User {} adds to cart firstly.", shoppingCartDto.getUserid());
            ShoppingCart cart = shoppingCartRepository.save(new ShoppingCart().builder().userid(shoppingCartDto.getUserid()).
                    shoppingCartItems(new ArrayList<>()).build());
            ShoppingCartItem shoppingCartItem = dtoToShoppingCartItemEntity(shoppingCartDto.getOrderItem());
            cart.getShoppingCartItems().add(shoppingCartItem);
        }else {
            log.info("User {} updates cart.", shoppingCartDto.getUserid());

            AtomicReference<ShoppingCartItem> repetitionOrderItem = new AtomicReference<>();
            shoppingCart.getShoppingCartItems().forEach(shoppingCartItem -> {
                if (shoppingCartItem.getProductId().equals(shoppingCartDto.getOrderItem().getProductId())){
                    repetitionOrderItem.set(shoppingCartItem);
                }
            });
            if(repetitionOrderItem.get() != null){
                // handle repetition order item in cart
                repetitionOrderItem.get().setQuantity(repetitionOrderItem.get().getQuantity() + shoppingCartDto.getOrderItem().getQuantity());
            }else{
                // add new order item to cart
                ShoppingCartItem shoppingCartItem = dtoToShoppingCartItemEntity(shoppingCartDto.getOrderItem());
                shoppingCart.getShoppingCartItems().add(shoppingCartItem);
            }
        }

        return true;
    }

    @Override
    public ArrayList<OrderItemDto> getCart(UUID userid) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(userid).orElse(null);

        // handel user first goes to cart.
        if(shoppingCart == null) {
            shoppingCartRepository.save(ShoppingCart.builder().userid(userid).shoppingCartItems(new ArrayList<>()).build());
            return new ArrayList<>();
        }

        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();

        List<UUID> productIds = new ArrayList<>();
        shoppingCartItems.forEach(shoppingCartItem -> productIds.add(shoppingCartItem.getProductId()));
        List<String> images = serviceCall.getProductImage(productIds);
       ArrayList<OrderItemDto> orderItemDtos = (ArrayList<OrderItemDto>) shoppingCartItems.stream().map(orderItem -> OrderItemDto.builder()
                .productId(orderItem.getProductId()).name(orderItem.getName())
                .price(orderItem.getPrice()).quantity(orderItem.getQuantity())
                .build()).collect(Collectors.toList());

        for(int i=0; i<images.size(); i++){
            orderItemDtos.get(i).setImage(images.get(i));
        }
        return orderItemDtos;
    }

    @Override
    public void placeOrder(UUID userId, List<OrderItemDto> orderRequest) {
        List<OrderItem> orderItems = dtosToEntities(orderRequest);
        Order order = Order.builder()
                .userid(userId)
                .orderStatus(OrderStatus.InProcess)
                .build();
        order.setOrderItems(orderItems);
        orderRepository.save(order);
    }

    @Override
    public void resetCart(UUID userId, List<OrderItemDto> orderItemDtos) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(userId).orElse(null);
        if(shoppingCart == null){
            log.error("No shoppingCart found with this userid: {}.",userId);
            throw new RuntimeException("No shoppingCart found with this userid: " + userId);
        }
        // TODO: ADD checkbox on template of cart page to enable function of partial selection on placing order.
        // only delete ordered shopping cart item(s), not ordered shopping cart item(s) will remain.
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        List<ShoppingCartItem> remainedShoppingCartItems = new ArrayList<>();
        List<ShoppingCartItem> orderedShoppingCartItems = new ArrayList<>();
        shoppingCartItems.forEach(shoppingCartItem -> {
            boolean anyMatch = orderItemDtos.stream()
                    .anyMatch(orderItemDto -> orderItemDto.getProductId().compareTo(shoppingCartItem.getProductId()) == 0);
            if(!anyMatch) remainedShoppingCartItems.add(shoppingCartItem);
            else orderedShoppingCartItems.add(shoppingCartItem);
        });
        shoppingCart.setShoppingCartItems(remainedShoppingCartItems);
        orderedShoppingCartItems.forEach(orderedShoppingCartItem->shoppingCartItemRepository.deleteById(orderedShoppingCartItem.getId()));
    }

    @Override
    public List<OrderDto> getAllOrder(UUID uuid) {
        return entitiesToOrderDtos(orderRepository.findAllByUserid(uuid));
    }

    @Override
    @Transactional
    public void deleteCartItem(String userId, String productId) {
        UUID userId_uuid = UUID.fromString(userId);
        UUID productId_uuid = UUID.fromString(productId);
        ShoppingCart shoppingCart = shoppingCartRepository.findById(userId_uuid)
                .orElseThrow();

        AtomicReference<UUID> idOfShoppingCartItemToBeDeleted = new AtomicReference<>();

        List<ShoppingCartItem> remainedShoppingCartItem = shoppingCart.getShoppingCartItems().stream()
                .filter(shoppingCartItem -> {
                    boolean isRemained = shoppingCartItem.getProductId().compareTo(productId_uuid) != 0;
                    if(!isRemained) idOfShoppingCartItemToBeDeleted.set(shoppingCartItem.getId());
                    return isRemained;
                }).collect(Collectors.toList());

        if(idOfShoppingCartItemToBeDeleted == null){
            log.error("No shopping cart item found with this userid {} and productid {}.",userId, productId);
            throw new RuntimeException(String.format("No shopping cart item found with this userid {} and productid {}."
                    ,userId, productId));
        }

        shoppingCart.setShoppingCartItems(remainedShoppingCartItem);
        shoppingCartItemRepository.deleteById(idOfShoppingCartItemToBeDeleted.get());
    }


    private List<OrderItem> dtosToEntities(List<OrderItemDto> orderRequest) {
        return orderRequest.stream().map(orderItemDto -> dtoToEntity(orderItemDto)).collect(Collectors.toList());
    }

    private List<OrderDto> entitiesToOrderDtos(List<Order> orders){
        return orders.stream().map(order -> entityToDto(order)).collect(Collectors.toList());
    }

    private List<OrderItemDto> entitiesToDtos(List<OrderItem> orderItems){
        return orderItems.stream().map(orderItem -> entityToDto(orderItem)).collect(Collectors.toList());
    }

    private OrderDto entityToDto(Order order){
        return OrderDto.builder().orderId(order.getOrderId())
                .orderItems(entitiesToDtos(order.getOrderItems()))
                .orderStatus(order.getOrderStatus()).build();
    }

    private OrderItemDto entityToDto(OrderItem orderItem){
        return OrderItemDto.builder().productId(orderItem.getProductId())
                .name(orderItem.getName()).price(orderItem.getPrice())
                .quantity(orderItem.getQuantity()).build();
    }

    private OrderItem dtoToEntity(OrderItemDto orderItemDto){
        return OrderItem.builder().productId(orderItemDto.getProductId())
                .name(orderItemDto.getName())
                .price(orderItemDto.getPrice())
                .quantity(orderItemDto.getQuantity())
                .build();
    }

    private ShoppingCartItem dtoToShoppingCartItemEntity(OrderItemDto orderItemDto){
        return ShoppingCartItem.builder().productId(orderItemDto.getProductId())
                .name(orderItemDto.getName())
                .price(orderItemDto.getPrice())
                .quantity(orderItemDto.getQuantity())
                .build();
    }


}
