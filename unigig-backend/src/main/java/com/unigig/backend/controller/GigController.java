package com.unigig.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unigig.backend.dto.CreateGigRequest;
import com.unigig.backend.dto.GigResponse;
import com.unigig.backend.service.GigService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gigs")
@RequiredArgsConstructor
public class GigController {
    private final GigService gigService;

    @PostMapping 
    public ResponseEntity<GigResponse> createGig(
        @Valid @RequestBody CreateGigRequest request,
        Authentication authentication
    ){
        GigResponse response = gigService.createGig(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
