package com.example.ui_kafka_sf.sf;
import jakarta.validation.constraints.NotBlank;
/** Payload sent to Kafka (Redpanda) -> Salesforce. */
public record SfEvent(@NotBlank String fullName, @NotBlank String email, String message) {}
