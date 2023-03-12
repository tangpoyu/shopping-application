package com.example.inventoryservice.web.service;

import com.example.inventoryservice.persistence.model.Inventory;
import com.example.inventoryservice.persistence.repository.InventoryRepository;
import com.example.inventoryservice.web.dto.InventoryDto;
import com.example.inventoryservice.web.dto.InventoryNotEnoughDto;
import com.example.inventoryservice.web.dto.OrderItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class DataManipulateWithDatabase implements DataManipulate{

//        @Autowired
//    private StreamBridge streamBridge;


    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceCall serviceCall;

    @Override
    public Integer isInStock(UUID productId) {
        Inventory inventory = inventoryRepository.findById(productId).orElse(null);
        if(inventory==null){
            log.error("No Inventory be found with this product id {}.",productId);
            return -1;
        }
        return inventory.getQuantity();
    }
    @Override
    @Transactional //FIXME: must make sure that any operation is successful before the quantity of inventory is reduced.
    public List<InventoryNotEnoughDto> isAllInStock(List<OrderItemDto> products) {
        List<InventoryNotEnoughDto> inventoriesIsNotEnough = new ArrayList<>();

        products.forEach(product -> {
            Inventory inventory = inventoryRepository.findById(product.getProductId()).orElse(null);
            if(inventory ==  null) {
                log.error("No inventory found with this product id {}.", product.getProductId());
                throw new RuntimeException("No inventory found with this product id " +product.getProductId() + ".");
            }
            if(inventory.getQuantity() < product.getQuantity()) {
                inventoriesIsNotEnough.add(InventoryNotEnoughDto.builder().productName(product.getName())
                        .quantity(inventory.getQuantity()).build());
            }
        });

        if(!inventoriesIsNotEnough.isEmpty()){
            // inventory is not enough
            return inventoriesIsNotEnough;
        }

        // inventory is enough
        products.forEach(product -> {
            Inventory inventory = inventoryRepository.findById(product.getProductId()).orElse(null);
            inventory.setQuantity(inventory.getQuantity() - product.getQuantity());
        });

        return new ArrayList<>();
    }

    @Override
    public List<Inventory> getAllInventory() {
       return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public UUID addInventory(String product, MultipartFile image) {
        UUID productId = null;
        try{
            log.info("Save inventory in inventory-service");
            String[] data = product.split(",");
            Inventory inventory = Inventory.builder()
                    .quantity(Integer.valueOf(data[3]))
                    .build();
            productId = inventoryRepository.save(inventory).getProductId();
        }catch (Exception e){
            log.info("Save inventory in inventory-service is failed.");
            throw new RuntimeException(e);
        }
        log.info("Save inventory {} in inventory-service successfully.", productId);

        product = productId + "," + product;
        log.info("Call product-service save product data and product image, productId is {}", productId);
        serviceCall.saveProduct(image,product);
        log.info("Call product-service save product data and product image, productId is {} successfully", productId);
        // inventoryRequest.setProductId(productId);
       // streamBridge.send("addproduct-out-0",inventoryRequest);

        return productId;
    }

    private InventoryDto entityToDto(Inventory inventory){
        return InventoryDto.builder().productId(inventory.getProductId())
                .quantity(inventory.getQuantity()).build();
    }
}
