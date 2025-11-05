package com.example.ui_kafka_sf.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Login response with token and user info for UI. */
public record TokenResp(String token, long expiresInSec, String username, @NotBlank String role) {}
