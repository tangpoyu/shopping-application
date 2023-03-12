package com.example.inventoryservice.web.controller;

import com.example.inventoryservice.persistence.model.Inventory;
import com.example.inventoryservice.web.dto.InventoryNotEnoughDto;
import com.example.inventoryservice.web.dto.OrderItemDto;
import com.example.inventoryservice.web.service.DataManipulate;
import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private DataManipulate dataManipulate; // depend on abstract


    @PutMapping("/isAllInStock")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('admin','user')")
//    @Observed(name = "is.allInStock", contextualName = "isAllInStock")
    public List<InventoryNotEnoughDto> isAllInStock(@RequestBody List<OrderItemDto> products){
        return dataManipulate.isAllInStock(products);
    }

    @GetMapping("/howManyInventory/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('user')")
//    @Observed(name = "how.many.inventory", contextualName = "howManyInventory")
    public Integer howManyInventory(@PathVariable("id") String productId){
        return dataManipulate.isInStock(UUID.fromString(productId));
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('admin')")
//    @Observed(name = "get.all.inventory", contextualName = "getAllInventoryRespons")
    public List<Inventory> getAllInventoryResponse(){
        return dataManipulate.getAllInventory();
    }

    @PostMapping(value = "/add")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('admin')")
//    @Observed(name = "add.inventory",contextualName = "addInventory")
    public UUID addInventory(@RequestParam("product") String product
            , @RequestParam("image") MultipartFile image) throws IOException {
        UUID productId = dataManipulate.addInventory(product, image);
        return productId;
    }



//    @PostMapping
//    @ResponseStatus(HttpStatus.OK)
//    public UUID modifyInventory(){
//
//    }

}
