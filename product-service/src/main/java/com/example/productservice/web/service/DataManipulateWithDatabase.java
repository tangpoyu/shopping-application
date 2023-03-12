package com.example.productservice.web.service;

import com.example.productservice.persistence.entity.Product;
import com.example.productservice.persistence.repository.ProductRepository;
import com.example.productservice.web.dto.ProductResponse;
import jakarta.persistence.criteria.CriteriaBuilder;
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
public class DataManipulateWithDatabase implements DataManipulate{

    @Autowired
    private ProductRepository productRepository;

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String IMAGE_DIR = "product_image";

    @Override
    public void createProduct(GenericMessage msg) {
        String payload = (String) msg.getPayload();
        try {
            JSONObject jsonObject = new JSONObject(payload);
            Integer price = (Integer) jsonObject.get("price");
            Product product = Product.builder()
                    .id(UUID.fromString((String) jsonObject.get("productId")))
                    .name((String) jsonObject.get("productName"))
                    .description((String) jsonObject.get("description"))
                    .price(BigDecimal.valueOf(price.doubleValue()))
                    .build();
            Product saved_product = productRepository.save(product);
            log.info("Product {} is saved", product.getId());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

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

        File dir = new File(USER_HOME, IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Path path = Paths.get(USER_HOME,IMAGE_DIR,data[0]+"." +image.getContentType().split("/")[1]);
       // Path path = Paths.get(folder,"product-service","src","main","resources","image", data[0]+"." +image.getContentType().split("/")[1]);
        try {
            Files.write(path,image.getBytes());
            log.info("Save product image {} in product-service successfully",data[0]);
        } catch (IOException e) {
            log.info("Save product image {} in product-service is failed",data[0]);
            throw new RuntimeException(String.format("Save product image {} in product-service is failed",data[0]));
        }
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

        return products.stream().map(product -> getImageBase64String(product)).collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProduct(String productId) {
        log.info("Get product {} from database",productId);
        Product product = productRepository.findById(UUID.fromString(productId)).orElseThrow(
                ()->new RuntimeException("No product found with this id: " + productId));

           return productToDto(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List products = productRepository.findAll().stream()
                .map(this::productToDto).collect(Collectors.toList());
        return products;
    }

    public ProductResponse productToDto(Product product){
        String bytes = getImageBase64String(product);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(bytes)
                .build();
    }

    public String getImageBase64String(Product product){
        byte[] bytes = null;

        try {
            log.info("Get product image {} from resource folder.", product.getId());
            Path path= Paths.get(USER_HOME,IMAGE_DIR, product.getId().toString()+product.getImageExtension());
//            ClassPathResource image = new ClassPathResource(Paths.get("image", product.getId().toString()).toString()+".jpeg");
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            log.info("Get product image {} from resource folder if failed", product.getId());
            throw new RuntimeException(e);
        }
        log.info("Get product image {} from resource folder successfully.", product.getId());
        return Base64.getEncoder().encodeToString(bytes);
    }
}
