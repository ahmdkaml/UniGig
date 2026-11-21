package com.unigig.backend.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unigig.backend.dto.CategoryResponse;
import com.unigig.backend.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // Read-only for any authenticated user: the post-gig dropdown and the marketplace filter need it.
    // Add/remove (admin only) belongs to the category-management story (will be implemented in sprint 2).
    @GetMapping
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(CategoryResponse::from)
                .toList();
    }
}