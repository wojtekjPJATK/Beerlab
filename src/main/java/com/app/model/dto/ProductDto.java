package com.app.model.dto;

import com.app.model.ProductTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String description;
    private String imgUrl;
    private String brand;
    private Double price;
    private Integer quantity;
    private Double minimalPrice;
    private ProductTypes productType;
}
