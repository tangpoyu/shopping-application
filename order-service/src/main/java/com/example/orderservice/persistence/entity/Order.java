package com.example.orderservice.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    private UUID orderId;

    private UUID userid;
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    private OrderStatus orderStatus;

}
