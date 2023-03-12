package com.example.client.dto;

import com.example.client.validator.OrderValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDtoHolder {

    @OrderValidation
    private List<OrderItemDto> orderItems;
    private ArrayList<Boolean> selectedRows;
}
