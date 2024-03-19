package com.example.ordering.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ItemResDto {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private String imagePath;
    private String category;

}
