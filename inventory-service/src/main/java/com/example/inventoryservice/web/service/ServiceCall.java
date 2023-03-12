package com.example.inventoryservice.web.service;



import com.example.inventoryservice.web.dto.InventoryDto;
import org.springframework.web.multipart.MultipartFile;

public interface ServiceCall {
   Boolean saveProduct(MultipartFile image, String product);
}
