package com.example.inventoryservice.web.service;


import com.example.inventoryservice.web.dto.InventoryDto;
import com.example.inventoryservice.web.util.ExtractToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@Slf4j
public class ServiceCallWithWebClient implements ServiceCall{

    @Autowired
    private WebClient.Builder appWebClientBuilder;


    @Override
    public Boolean saveProduct(MultipartFile image, String product) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("product",product);
        builder.part("image",image.getResource());
        return appWebClientBuilder.build().post().uri("http://product-service/api/product/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(header -> header.setBearerAuth(ExtractToken.getToken()))
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
