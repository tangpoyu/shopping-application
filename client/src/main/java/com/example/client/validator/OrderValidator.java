package com.example.client.validator;

import com.example.client.dto.OrderItemDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class OrderValidator implements ConstraintValidator<OrderValidation, List<OrderItemDto>> {
    @Override
    public boolean isValid(List<OrderItemDto> orderItems, ConstraintValidatorContext context) {

        if(orderItems.stream().anyMatch(orderItemDto -> {
            return orderItemDto.getQuantity() == null || orderItemDto.getQuantity() <= 0
                    || orderItemDto.getQuantity() % 1 != 0;
        })){
            return false;
        }
        return true;
    }
}
