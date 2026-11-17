package com.unigig.backend.controller;

import com.unigig.backend.dto.FreelancerProfileRequest;
import com.unigig.backend.dto.FreelancerProfileResponse;
import com.unigig.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    

    // 1. Logged-in freelancer views their own profile (ID comes from JWT)
    @GetMapping("/me")
    public ResponseEntity<FreelancerProfileResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); // or userId if your JwtAuthFilter sets User/userId as principal
        return ResponseEntity.ok(profileService.getProfileByEmail(email));
    }

    // 2. Logged-in freelancer creates or updates their own profile (ID comes from JWT, fields in JSON)
    @PutMapping("/me")
    public ResponseEntity<FreelancerProfileResponse> upsertMyProfile(
            Authentication authentication,
            @Valid @RequestBody FreelancerProfileRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.upsertProfile(email, request));
    }

    // 3. Clients or visitors view a specific freelancer's public profile (ID in URL path)
    @GetMapping("/{userId}")
    public ResponseEntity<FreelancerProfileResponse> getProfileById(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }
}