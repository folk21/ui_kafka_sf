package com.example.ui_kafka_sf.auth.dto;

import com.example.ui_kafka_sf.auth.Role;

/** Domain event published when a user has been registered. */
public record UserRegisteredEvent(String username, Role role, long occurredAtEpochMillis) {}
