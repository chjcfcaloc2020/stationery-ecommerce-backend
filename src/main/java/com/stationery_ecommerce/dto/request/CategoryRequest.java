package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category's name is not blank")
    private String name;

    @NotBlank(message = "Category's icon is not blank")
    private String icon;

    @NotBlank(message = "Category's color is not blank")
    private String color;

    @NotBlank(message = "Category's sortOrder is not blank")
    private Integer sortOrder;
}
