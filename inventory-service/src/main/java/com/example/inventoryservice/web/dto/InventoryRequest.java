package com.example.inventoryservice.web.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequest {
    private UUID productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private MultipartFile image;
}
