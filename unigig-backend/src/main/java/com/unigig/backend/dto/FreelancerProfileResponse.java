package com.unigig.backend.dto;

import com.unigig.backend.entity.FreelancerProfile;

import java.time.Instant;
import java.util.List;

public record FreelancerProfileResponse(
        Long userId,
        String fullName,
        String title,
        String bio,
        List<String> skills,
        String portfolioUrl,
        Instant updatedAt
) {
    public static FreelancerProfileResponse from(FreelancerProfile profile) {
        return new FreelancerProfileResponse(
                profile.getUserId(),
                profile.getUser().getFullName(),
                profile.getTitle(),
                profile.getBio(),
                profile.getSkills(),
                profile.getPortfolioUrl(),
                profile.getUpdatedAt()
        );
    }
}