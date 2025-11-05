package com.example.ui_kafka_sf.course;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "course")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Course {

  @Id
  private String id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(length = 4000)
  private String description;

  @Enumerated(EnumType.STRING)
  private Term term;

  /** Academic year, e.g. "2025" */
  private String year;

  private OffsetDateTime expired;

  @Builder.Default
  private boolean started = false;

  /** Instructor user id (username or external id) */
  @Column(name = "instructor_id")
  private String instructorId;
}
