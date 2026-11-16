package com.unigig.backend.dto;

import com.unigig.backend.entity.UserRole;

public record LoginResponse(
        String token,
        Long id,
        String fullName,
        String email,
        UserRole role
) {}