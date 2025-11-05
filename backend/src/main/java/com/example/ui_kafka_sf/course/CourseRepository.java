package com.example.ui_kafka_sf.course;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** CourseRepository belongs to the Course domain (entity/repository/DTO for course operations). */
public interface CourseRepository extends JpaRepository<Course, String> {
  List<Course> findByInstructorId(String instructorId);
}
