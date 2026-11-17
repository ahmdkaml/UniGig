package com.unigig.backend.service;

import com.unigig.backend.dto.FreelancerProfileRequest;
import com.unigig.backend.dto.FreelancerProfileResponse;
import com.unigig.backend.entity.FreelancerProfile;
import com.unigig.backend.entity.User;
import com.unigig.backend.entity.UserRole;
import com.unigig.backend.repository.FreelancerRepository;
import com.unigig.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final FreelancerRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FreelancerProfileResponse getProfileByEmail(String email) {
        User user = findUserByEmail(email);
        FreelancerProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Freelancer profile not found"));
        return FreelancerProfileResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public FreelancerProfileResponse getProfileByUserId(Long userId) {
        FreelancerProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Freelancer profile not found"));
        return FreelancerProfileResponse.from(profile);
    }

    @Transactional
    public FreelancerProfileResponse upsertProfile(String email, FreelancerProfileRequest request) {
        User user = findUserByEmail(email);

        if (user.getRole() != UserRole.FREELANCER) {
            throw new AccessDeniedException("Only freelancers can create or update a freelancer profile");
        }

        List<String> normalizedSkills = request.skills().stream()
                .map(String::trim)
                .distinct()
                .toList();

        FreelancerProfile profile = profileRepository.findById(user.getId())
                .orElseGet(() -> FreelancerProfile.builder()
                        .user(user)
                        .build());

        profile.setTitle(request.title().trim());
        profile.setBio(request.bio() != null ? request.bio().trim() : null);
        profile.setSkills(normalizedSkills);
        profile.setPortfolioUrl(
                request.portfolioUrl() != null && !request.portfolioUrl().isBlank()
                        ? request.portfolioUrl().trim()
                        : null
        );

        FreelancerProfile saved = profileRepository.save(profile);
        return FreelancerProfileResponse.from(saved);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}