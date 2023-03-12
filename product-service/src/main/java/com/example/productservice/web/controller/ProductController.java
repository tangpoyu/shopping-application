package com.example.productservice.web.controller;

import com.example.productservice.web.dto.ProductRequest;
import com.example.productservice.web.dto.ProductResponse;
import com.example.productservice.web.service.DataManipulate;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.GetExchange;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/product")
@Slf4j
public class ProductController {

    @Autowired
    private DataManipulate dataManipulate; // depend on abstract


//    @Bean
//    public Consumer<Message<String>> addproduct(){
//        log.info("Add a new Product.");
//        return msg -> {
//            dataManipulate.createProduct((GenericMessage) msg);
//        };
//    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('admin')")
//    @Observed(name = "add.product",contextualName = "addProduct")
    public Boolean addProduct(@RequestParam("product") String product
            , @RequestParam("image") MultipartFile image){
        log.info("Receive inventory-service request about saving product data and image data successfully");
        dataManipulate.createProduct(product,image);
        return true;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('admin','user')")
//    @Observed(name = "get.product",contextualName = "getProduct")
    public ProductResponse getProduct(@PathVariable("id") String productId){
        log.info("product-service getProduct() receive call with productId {}", productId);
        return dataManipulate.getProduct(productId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('admin','user')")
//    @Observed(name = "get.all.products",contextualName = "getAllProducts")
    public List<ProductResponse> getAllProducts(){
        return dataManipulate.getAllProducts();
    }

    @GetMapping("/images")
//    @Observed(name = "get.productImages",contextualName = "getProductImages")
    public List<String> getProductImages(@RequestParam("productIds") List<UUID> productIds) {
        return dataManipulate.getProductImages(productIds);
    }


}
