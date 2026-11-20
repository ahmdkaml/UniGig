package com.unigig.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.unigig.backend.dto.CreateGigRequest;
import com.unigig.backend.dto.GigResponse;
import com.unigig.backend.entity.Category;
import com.unigig.backend.entity.Gig;
import com.unigig.backend.entity.GigStatus;
import com.unigig.backend.entity.User;
import com.unigig.backend.entity.UserRole;
import com.unigig.backend.repository.CategoryRepository;
import com.unigig.backend.repository.GigRepository;
import com.unigig.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GigService {

    private static final int PAGE_SIZE = 10;

    private final GigRepository gigRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public GigResponse createGig(CreateGigRequest request, Authentication authentication) {
        User client = resolveAuthenticatedUser(authentication);

        if (client.getRole() != UserRole.CLIENT) {
            throw new AccessDeniedException("Only clients can post gigs");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));

        Gig gig = Gig.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .status(GigStatus.OPEN)
                .client(client)
                .category(category)
                .build();

        Gig savedGig = gigRepository.save(gig);
        return GigResponse.from(savedGig);
    }

    @Transactional(readOnly = true)
    public Page<GigResponse> searchOpenGigs(String keyword, Long categoryId, int page) {
        int safePage = Math.max(0, page);
        String normalizedKeyword = (keyword == null) ? null : keyword.trim();
        Pageable pageable = PageRequest.of(safePage, PAGE_SIZE);

        return gigRepository
                .searchByStatusAndFilters(GigStatus.OPEN, categoryId, normalizedKeyword, pageable)
                .map(GigResponse::from);
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}