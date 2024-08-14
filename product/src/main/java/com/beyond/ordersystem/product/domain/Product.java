package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    private Integer price;

    private Integer stockQuantity;

    private String imagePath;

    public void updateImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public void updateStockQuantity(Integer quantity) {this.stockQuantity -= quantity;}
}
