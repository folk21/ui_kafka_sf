package com.example.ui_kafka_sf.auth.dto;
import com.example.ui_kafka_sf.auth.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
/** Self-registration request (INSTRUCTOR or STUDENT). */
public record RegisterReq(@NotBlank String username, @NotBlank String password, @NotNull Role role) {}
