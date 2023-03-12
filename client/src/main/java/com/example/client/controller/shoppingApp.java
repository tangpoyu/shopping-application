package com.example.client.controller;


import com.example.client.dto.*;
import com.example.client.service.ServiceCaller;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.math.BigDecimal;
import java.util.*;


@Controller
@Slf4j
public class shoppingApp {

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private MeterRegistry meterRegistry;


    @GetMapping("all/product")
    public String getAllProduct(Model model,HttpSession session){
        log.info("Get all product starts.");
        meterRegistry.counter("get.total").increment();
        try{
            ArrayList<ProductResponse> productResponses = serviceCaller.getAllProduct();
            session.setAttribute("products",productResponses);
            model.addAttribute("products",productResponses);
        } catch (WebClientResponseException e){
            model.addAttribute("error_msg", e.getMessage());
            model.addAttribute("products",new ArrayList());
        } catch (Exception e) {
            model.addAttribute("error_msg", "Something error.");
            model.addAttribute("products",new ArrayList());
        }
        return "products";
    }

    @GetMapping("/inventory/{id}")
    public String getInventoryDetail(Model model, @PathVariable(value = "id") String id, HttpSession session){
        try{
            ProductResponse selectedProduct = null;
            ArrayList<ProductResponse> productResponses = (ArrayList<ProductResponse>) session.getAttribute("products");
            if(productResponses != null) {
                // see product from product.html (user or admin)
                UUID UUIDid = UUID.fromString(id);
                selectedProduct = productResponses.stream().filter(productResponse -> productResponse.getId().equals(UUIDid)).findFirst().orElse(null);

                if(selectedProduct == null) {
                    model.addAttribute("products",productResponses);
                    return "products";
                }
            } else {
                // see product from inventories.html (admin)
                selectedProduct = serviceCaller.getProduct(id);
            }

            session.setAttribute("inventory",selectedProduct);
            model.addAttribute("inventory", selectedProduct);
        }catch (WebClientResponseException e){
            model.addAttribute("error_msg", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error_msg", "Something error.");
        }
        return "inventory";
    }

    @GetMapping("all/inventory")
    @PreAuthorize("hasAuthority('admin')")
    public String getAllInventory(Model model,HttpSession session){
        try {
            ArrayList<InventoryDto> allInventory = serviceCaller.getAllInventory();
            session.setAttribute("inventories",allInventory);
            model.addAttribute("inventories",allInventory);
        }catch (WebClientResponseException e){
            model.addAttribute("error_msg", e.getMessage());
            model.addAttribute("inventories",new ArrayList());
        }
        return "inventories";
    }

    @GetMapping("/add/product")
    @PreAuthorize("hasAuthority('admin')")
        public String addProduct(Model model){
        model.addAttribute("product",new ProductRequest(null,"","", BigDecimal.ZERO,0,null));
        return "addProduct";
    }

    @PostMapping(value ="/add/product", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }) // ************************ BindingResult must after the object that it belongs to. ***********************************
    @PreAuthorize("hasAuthority('admin')")
    public String addProduct(@Valid @ModelAttribute("product") ProductRequest productRequest
            , BindingResult result,  Model model){
        // validate input
        if(result.hasErrors()){
            model.addAttribute("product",productRequest);
            return "addProduct";
        }

        try{
            UUID productId = serviceCaller.addProduct(productRequest);
            model.addAttribute("success_msg","You have add product " + productId + " successfully.");
        }catch (WebClientResponseException e){
            model.addAttribute("error_msg", e.getMessage());
        }


        return "addProduct";
    }

    @PostMapping("/addToCart/{id}")
    public String addToCart(@PathVariable("id") String id,@RequestParam("quantity") String quantity, Model model,HttpSession session){
        ProductResponse product = (ProductResponse) session.getAttribute("inventory");
        Integer requestQuantity = 0;

        try {
            requestQuantity = Integer.valueOf(quantity);
        }catch (Exception e){
            model.addAttribute("error_msg", "Please provide a integer of quantity of product.");
            model.addAttribute("inventory", product);
            return "inventory";
        }

        if(requestQuantity < 1){
            model.addAttribute("error_msg", "quantity can't less than 1.");
            model.addAttribute("inventory", product);
            return "inventory";
        }

        if(requestQuantity%1 != 0){
            model.addAttribute("error_msg", "quantity must be Integer.");
            model.addAttribute("inventory", product);
            return "inventory";
        }

        Integer inventoryQuantity = serviceCaller.howManyInventory(id);

        if(inventoryQuantity.equals(-1)){
            model.addAttribute("error_msg","Sorry something error in System, please later");
            model.addAttribute("inventory", product);
            return "inventory";
        }

        if(inventoryQuantity <  requestQuantity){
            // don't have enough inventory
            log.warn("We don't have enough inventory with this product {}.", id);
            model.addAttribute("error_msg","Sorry we only have " + inventoryQuantity + " stock remaining." );
            model.addAttribute("inventory", product);
            return "inventory";
        }

        // have enough inventory to add to shopping cart
        String userid = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            log.info("{} Call order-service to add product to cart.",userid);
            serviceCaller.addToShoppingCart(requestQuantity,userid,product);
            model.addAttribute("success_msg", "Add successfully.");
        }catch (WebClientResponseException e){
            log.info("{} Call order-service to add product to cart is failed.",userid);
            model.addAttribute("error_msg", e.getMessage());
        }
        model.addAttribute("inventory", product);
        return "inventory";
    }

    @GetMapping("/cart")
    @PreAuthorize("hasAuthority('user')")
    public String goToCart(Model model,HttpSession session){
        String userid = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            ArrayList<OrderItemDto> orderItemDtos = serviceCaller.getCartOrderItems(userid);
            OrderItemDtoHolder orderItemDtoHolder = OrderItemDtoHolder.builder().orderItems(orderItemDtos).build();
            session.setAttribute("cart",orderItemDtoHolder);
            model.addAttribute("cart",orderItemDtoHolder);
        }catch (Exception e){
            log.error("Get cart items with this userid is failed {}",userid);
            model.addAttribute("cart",OrderItemDtoHolder.builder().orderItems(new ArrayList<>()).build());
            model.addAttribute("error_msg", e.getMessage());
        }
       return "cart";
    }

    @PostMapping("/placeOrder")
    @PreAuthorize("hasAuthority('user')")
    public String placeOrder(@Valid @ModelAttribute("cart") OrderItemDtoHolder cart,
                             BindingResult result, Model model,HttpSession session){

        // validate input
        if(result.hasErrors()){
            model.addAttribute("cart",cart);
            return "cart";
        }

        try{
            String userid = SecurityContextHolder.getContext().getAuthentication().getName();

            List<OrderItemDto> selectedOrderItem = new ArrayList<>();
            List<OrderItemDto> nonSelectedOrderItem =  new ArrayList<>();
            for(int i=0; i<cart.getOrderItems().size(); i++){
                if(cart.getSelectedRows().get(i)){
                    selectedOrderItem.add(cart.getOrderItems().get(i));
                }else{
                    nonSelectedOrderItem.add(cart.getOrderItems().get(i));
                }
            }

            if(selectedOrderItem.isEmpty()){
                model.addAttribute("error_msg","You don't select any product to place.");
                model.addAttribute("cart",cart);
                return "cart";
            }

            List<InventoryNotEnoughDto> inventoryNotEnoughDtos = serviceCaller.placeOrder(
                    userid, selectedOrderItem
            );

            if(!inventoryNotEnoughDtos.isEmpty()) {
                String  error_msg = "Sorry, ";

                for(int i=0; i<inventoryNotEnoughDtos.size(); i++){
                    InventoryNotEnoughDto inventoryNotEnoughDto = inventoryNotEnoughDtos.get(i);
                    error_msg += inventoryNotEnoughDto.getProductName() + " has only "
                            + inventoryNotEnoughDto.getQuantity() + " remaining. ";
                }

                model.addAttribute("error_msg",error_msg);
                model.addAttribute("cart",cart);
            }else {
                model.addAttribute("success_msg","Place successfully, your order is in process.");
                model.addAttribute("cart",OrderItemDtoHolder.builder().orderItems(nonSelectedOrderItem).build());
            }
        }catch (Exception e){
            model.addAttribute("error_msg",e.getMessage());
            model.addAttribute("cart",cart);
        }

        return "cart";
    }

    @GetMapping("/cart/delete/{id}")
    @PreAuthorize("hasAuthority('user')")
    public String cartItemDelete(Model model, @PathVariable("id") String productId){
        try{
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            ArrayList<OrderItemDto> orderItemDtos = serviceCaller.deleteCartItem(userId,productId);
            model.addAttribute("cart",OrderItemDtoHolder.builder().orderItems(orderItemDtos).build());
        }catch (Exception e){
            model.addAttribute("error_msg",e.getMessage());
            model.addAttribute("cart",OrderItemDtoHolder.builder().orderItems(new ArrayList<>()).build());
        }
        return "cart";
    }

    @GetMapping("/orderTracking")
    @PreAuthorize("hasAuthority('user')")
    public String trackOrder(Model model, HttpSession session){
        String userid = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            List<OrderDto> orderDtos = serviceCaller.orderTrack(userid);
            session.setAttribute("orders",orderDtos);
            model.addAttribute("orders", orderDtos);
        }catch (WebClientResponseException e){
            log.error("Something error when this userId {} tracked order.", userid);
            model.addAttribute("error_msg",e.getMessage());
        }
        catch (Exception e){
            log.error("Something error when this userId {} tracked order.", userid);
            model.addAttribute("error_msg","Sorry something error in server.");
        }
        return "orderTrack";
    }

    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable("id") String orderId,Model model, HttpSession session){
        List<OrderDto> orderDtos = (List<OrderDto>) session.getAttribute("orders");
        try{
            OrderDto first = orderDtos.stream()
                    .filter(orderDto -> orderDto.getOrderId().equals(UUID.fromString(orderId)))
                    .findFirst().orElse(null);
            if(first == null){
                model.addAttribute("error_msg","Something error.");
                model.addAttribute("orders", orderDtos);
                return "orderTrack";
            }
            model.addAttribute("orderItems",first.getOrderItems());
        }catch (Exception e){
            model.addAttribute("error_msg","Something error.");
            model.addAttribute("orders", orderDtos);
            return "orderTrack";
        }
        return "orderItems";
    }


}
