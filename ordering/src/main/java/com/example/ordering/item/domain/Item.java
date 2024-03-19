package com.example.ordering.item.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
    private String category;
    private String imagePath;
    @Builder.Default
    private String delYn = "N";

    @CreationTimestamp
    LocalDateTime createdTime;
    @UpdateTimestamp
    LocalDateTime updatedTime;

    public void deleteItem() {
        this.delYn = "Y";
    }


    public void updateStockQuantity(int newQuantity) {
        this.stockQuantity = newQuantity;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void updateItem(String name, String category, int price, int stockQuantity, String imagePath) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.imagePath = imagePath;
    }
}
