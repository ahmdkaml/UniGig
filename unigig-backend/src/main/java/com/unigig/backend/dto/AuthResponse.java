package com.unigig.backend.dto;

import com.unigig.backend.entity.UserRole;

public record AuthResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {}