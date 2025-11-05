package com.example.ui_kafka_sf.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Login request. */
public record LoginReq(@NotBlank String username, @NotBlank String password) {}
