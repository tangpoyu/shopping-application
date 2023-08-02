package com.example.productservice.web.util;

import com.example.productservice.persistence.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class GetImage {
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String IMAGE_DIR = "product_image";


    public void saveImage(String productId, MultipartFile image){
        File dir = new File(USER_HOME, IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Path path = Paths.get(USER_HOME,IMAGE_DIR,productId+"." +image.getContentType().split("/")[1]);
        // Path path = Paths.get(folder,"product-service","src","main","resources","image", data[0]+"." +image.getContentType().split("/")[1]);
        try {
            Files.write(path,image.getBytes());
            log.info("Save product image {} in product-service successfully",productId);
        } catch (IOException e) {
            log.info("Save product image {} in product-service is failed",productId);
            throw new RuntimeException(String.format("Save product image {} in product-service is failed",productId));
        }
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
