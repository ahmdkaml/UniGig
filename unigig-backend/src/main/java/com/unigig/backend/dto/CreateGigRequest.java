package com.unigig.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateGigRequest(
    @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Budget is required")
        @Positive(message = "Budget must be greater than 0")
        BigDecimal budget
) {
}