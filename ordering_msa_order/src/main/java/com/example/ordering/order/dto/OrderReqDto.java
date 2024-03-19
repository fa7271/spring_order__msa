package com.example.ordering.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderReqDto {
    private Long itemId;
    private int count;
}
