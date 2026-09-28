package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category's name is not blank")
    private String name;

    @NotBlank(message = "Category's description is not blank")
    private String description;

    @NotBlank(message = "Category's icon is not blank")
    private String icon;

    @NotBlank(message = "Category's color is not blank")
    private String color;

    @NotNull
    private Integer sortOrder;
}
