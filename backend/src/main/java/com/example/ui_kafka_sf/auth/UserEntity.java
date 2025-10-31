package com.example.ui_kafka_sf.auth;
/** Immutable user stored in DynamoDB. */
public record UserEntity(String username, String passwordHash, Role role) {}
