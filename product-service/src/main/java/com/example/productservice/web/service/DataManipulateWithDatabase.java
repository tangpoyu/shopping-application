package com.example.productservice.web.service;

import com.example.productservice.persistence.entity.Product;
import com.example.productservice.persistence.repository.ProductRepository;
import com.example.productservice.web.dto.ProductResponse;
import com.example.productservice.web.util.Converter;
import com.example.productservice.web.util.GetImage;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DataManipulateWithDatabase implements DataManipulate{

    private ProductRepository productRepository;
    private GetImage getImage;
    private Converter converter;

    @Override
    @Transactional
    public void createProduct(String product, MultipartFile image) {
        String[] data = product.split(",");
        log.info("Save product data {} in product-service",data[0]);
        try{
            // FIXME: bug
            productRepository.save(
                    Product.builder().id(UUID.fromString(data[0]))
                            .name(data[1]).description(data[2])
                            .price(BigDecimal.valueOf(Double.valueOf(data[3])))
                            .imageExtension( "."+image.getContentType().split("/")[1]).build());
        }catch (Exception e){
            log.info("Save product data {} in product-service is failed",data[0]);
            throw new RuntimeException(String.format("Save product data {} in product-service is failed, Exception: {}",data[0],e.getMessage()));
        }

        log.info("Save product data {} in product-service successfully",data[0]);
        log.info("Save product image {} in product-service",data[0]);
        getImage.saveImage(data[0],image);
    }

    @Override
    public List<String> getProductImages(List<UUID> productIds) {
        List<String> images = new ArrayList<>();
        List<Product> products = productIds.stream()
                .map(productId -> productRepository.findById(productId).orElseThrow(() -> {
                    log.error("No product found with this id {}",productId);
                    throw new RuntimeException(String.format("No product found with this id {}",productId));
                }))
                .collect(Collectors.toList());

        return products.stream().map(product -> getImage.getImageBase64String(product)).collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProduct(String productId) {
        log.info("Get product {} from database",productId);
        Product product = productRepository.findById(UUID.fromString(productId)).orElseThrow(
                ()->new RuntimeException("No product found with this id: " + productId));

           return converter.productToDto(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List products = productRepository.findAll().stream()
                .map(product -> converter.productToDto(product)).collect(Collectors.toList());
        return products;
    }
}
