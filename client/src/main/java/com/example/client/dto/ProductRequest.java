package com.example.client.dto;


import com.example.client.validator.ImageValidation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    private UUID productId;
    @NotEmpty
    private String productName;
    @NotEmpty
    @Size(max= 1000)
    private String description;
    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal price;
    @NotNull
    @DecimalMin(value = "1")
    private Integer quantity;
    @ImageValidation
    private MultipartFile image;
}
