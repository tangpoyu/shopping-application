package com.example.productservice.web.util;

import com.example.productservice.persistence.entity.Product;
import com.example.productservice.web.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Converter {

    @Autowired
    private GetImage getImage;

    public ProductResponse productToDto(Product product){
        String bytes = getImage.getImageBase64String(product);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(bytes)
                .build();
    }
}
