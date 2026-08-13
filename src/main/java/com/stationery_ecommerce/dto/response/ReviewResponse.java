package com.stationery_ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private String userFullName;
    private Integer rating;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
}
