package com.stationery_ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Product ID is not null")
    private Long productId;

    @NotNull(message = "Rating is not null")
    @Min(value = 1, message = "Rate score minimum is 1")
    @Max(value = 5, message = "Rate score maximum is 5")
    private Integer rating;

    @NotBlank(message = "Comment is not blank")
    @Size(max = 1000, message = "Comment maximum is 1000 characters")
    private String comment;
}
