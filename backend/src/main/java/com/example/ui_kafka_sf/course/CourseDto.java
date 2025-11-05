package com.example.ui_kafka_sf.course;

import java.time.OffsetDateTime;

/** CourseDto belongs to the Course domain (entity/repository/DTO for course operations). */
public record CourseDto(
    String id,
    String code,
    String name,
    String description,
    Term term,
    String year,
    OffsetDateTime expired,
    boolean started,
    String instructorId) {}
