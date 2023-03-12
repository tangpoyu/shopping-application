package com.example.orderservice.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    @Id
    private UUID userid;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ShoppingCartItem> shoppingCartItems;
}
