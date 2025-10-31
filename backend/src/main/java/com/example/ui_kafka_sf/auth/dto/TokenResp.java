package com.example.ui_kafka_sf.auth.dto;
/** Login response with token and user info for UI. */
public record TokenResp(String token, long expiresInSec, String username, String role) {}
