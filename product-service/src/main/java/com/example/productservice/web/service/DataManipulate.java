package com.example.productservice.web.service;

import com.example.productservice.web.dto.ProductResponse;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DataManipulate {
    void createProduct(GenericMessage msg);

    List<ProductResponse> getAllProducts();

    ProductResponse getProduct(String productId);

    void createProduct(String product, MultipartFile image);

    List<String> getProductImages(List<UUID> productIds);
}
