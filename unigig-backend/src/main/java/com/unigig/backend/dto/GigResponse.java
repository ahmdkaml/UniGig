package com.unigig.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.unigig.backend.entity.Gig;
import com.unigig.backend.entity.GigStatus;

public record GigResponse(
        Long id,
        String title,
        String description,
        BigDecimal budget,
        GigStatus status,
        Long categoryId,
        String categoryName,
        Long clientId,
        String clientName,
        Instant createdAt
) {
    public static GigResponse from(Gig gig) {
        return new GigResponse(
                gig.getId(),
                gig.getTitle(),
                gig.getDescription(),
                gig.getBudget(),
                gig.getStatus(),
                gig.getCategory().getId(),
                gig.getCategory().getName(),
                gig.getClient().getId(),
                gig.getClient().getFullName(),
                gig.getCreatedAt()
        );
    }
}